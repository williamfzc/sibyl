package com.williamfzc.sibyl.ext.spring.exporter;

import com.squareup.javapoet.*;
import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.ext.CommonUtils;
import com.williamfzc.sibyl.ext.spring.model.ServiceCase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Resource;
import javax.lang.model.element.Modifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

public class JUnitExporter extends BaseExporter {
    // create methods
    // create fields
    // create test class

    public MethodSpec case2Spec(ServiceCase serviceCase) {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder("test" + serviceCase.getMethodName())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);
        methodBuilder.addAnnotation(Test.class);

        String paramsStr = "";
        if (null != serviceCase.getParams()) {
            paramsStr =
                    serviceCase.getParams().stream()
                            .filter(Objects::nonNull)
                            .map(Parameter::getName)
                            .collect(Collectors.joining(", "));
        }
        methodBuilder.addCode(
                String.format(
                        "%s.%s(%s);\n",
                        CommonUtils.toLowerCaseForFirstLetter(
                                serviceCase.getServiceClazzLiberalName()),
                        serviceCase.getMethodName(),
                        paramsStr));
        return methodBuilder.build();
    }

    public List<MethodSpec> cases2Specs(Iterable<ServiceCase> serviceCases) {
        return StreamSupport.stream(serviceCases.spliterator(), false)
                .map(this::case2Spec)
                .collect(Collectors.toList());
    }

    public JavaFile specs2JavaFile(
            String packageName, String clazzName, Iterable<MethodSpec> methodSpecs) {
        TypeSpec.Builder clazzBuilder =
                TypeSpec.classBuilder("Test" + clazzName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // fields
        clazzBuilder.addField(
                FieldSpec.builder(
                                ClassName.get(packageName, clazzName),
                                CommonUtils.toLowerCaseForFirstLetter(clazzName))
                        .addAnnotation(Resource.class)
                        .build());

        // methods
        clazzBuilder.addMethods(methodSpecs);

        AnnotationSpec.Builder runWithBuilder =
                AnnotationSpec.builder(RunWith.class)
                        .addMember(
                                "value",
                                CodeBlock.builder().add("$T.class", SpringRunner.class).build());
        clazzBuilder.addAnnotation(runWithBuilder.build());

        TypeSpec cur = clazzBuilder.build();
        return JavaFile.builder(packageName, cur).build();
    }

    public JavaFile cases2JavaFile(
            String packageName, String clazzName, Iterable<ServiceCase> serviceCases) {
        return specs2JavaFile(packageName, clazzName, cases2Specs(serviceCases));
    }

    public List<JavaFile> cases2JavaFiles(Iterable<ServiceCase> serviceCases) {
        List<JavaFile> ret = new ArrayList<>();
        Map<String, List<ServiceCase>> packageDimMap =
                StreamSupport.stream(serviceCases.spliterator(), false)
                        .collect(
                                Collectors.groupingBy(
                                        ServiceCase::getServicePackageName, Collectors.toList()));
        for (String packageName : packageDimMap.keySet()) {
            List<JavaFile> cur = cases2JavaFiles(packageName, packageDimMap.get(packageName));
            ret.addAll(cur);
        }
        return ret;
    }

    public List<JavaFile> cases2JavaFiles(String packageName, Iterable<ServiceCase> serviceCases) {
        List<JavaFile> ret = new ArrayList<>();
        Map<String, List<ServiceCase>> clazzDimMap =
                StreamSupport.stream(serviceCases.spliterator(), false)
                        .collect(
                                Collectors.groupingBy(
                                        ServiceCase::getServiceClazzName, Collectors.toList()));
        for (String clazzName : clazzDimMap.keySet()) {
            JavaFile cur = cases2JavaFile(packageName, clazzName, clazzDimMap.get(clazzName));
            ret.add(cur);
        }
        return ret;
    }

    public List<String> cases2JavaFilesRaw(Iterable<ServiceCase> serviceCases) {
        return cases2JavaFiles(serviceCases).stream()
                .map(
                        javaFile -> {
                            String raw = javaFile.toString();
                            for (FieldSpec eachField : javaFile.typeSpec.fieldSpecs) {
                                String fullType = eachField.type.toString();
                                String clazzName = CommonUtils.fullPath2ClazzName(fullType);
                                raw = raw.replaceAll(" " + clazzName, " " + fullType);
                                break;
                            }
                            return raw;
                        })
                .collect(Collectors.toList());
    }
}
