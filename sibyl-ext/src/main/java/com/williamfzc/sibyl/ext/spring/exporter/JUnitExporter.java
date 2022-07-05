package com.williamfzc.sibyl.ext.spring.exporter;

import com.squareup.javapoet.*;
import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.ext.spring.model.ServiceCase;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.lang.model.element.Modifier;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

public class JUnitExporter extends BaseExporter {
    // create methods
    // create fields
    // create test class

    public MethodSpec case2Spec(ServiceCase serviceCase) {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder("test" + serviceCase.getMethodName())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);

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
                        serviceCase.getServiceClazzName(), serviceCase.getMethodName(), paramsStr));
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
        clazzBuilder.addMethods(methodSpecs);

        AnnotationSpec.Builder runWithBuilder =
                AnnotationSpec.builder(RunWith.class)
                        .addMember(
                                "value", CodeBlock.builder().add("$T.class", JUnit4.class).build());
        clazzBuilder.addAnnotation(runWithBuilder.build());

        TypeSpec cur = clazzBuilder.build();
        return JavaFile.builder(packageName, cur).build();
    }

    public JavaFile cases2JavaFile(
            String packageName, String clazzName, Iterable<ServiceCase> serviceCases) {
        return specs2JavaFile(packageName, clazzName, cases2Specs(serviceCases));
    }
}
