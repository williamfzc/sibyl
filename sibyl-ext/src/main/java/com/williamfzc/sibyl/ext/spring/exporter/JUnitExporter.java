package com.williamfzc.sibyl.ext.spring.exporter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.squareup.javapoet.*;
import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.ext.CommonUtils;
import com.williamfzc.sibyl.ext.spring.model.JUnitCaseFile;
import com.williamfzc.sibyl.ext.spring.model.TestedMethodModel;
import com.williamfzc.sibyl.ext.spring.model.UserCase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Resource;
import javax.lang.model.element.Modifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

public class JUnitExporter extends BaseExporter {
    // create methods
    // create fields
    // create test class
    private final Gson gson = new Gson();

    public List<MethodSpec> case2Spec(TestedMethodModel serviceCase) {
        if (userCaseData.containsKey(serviceCase.getMethodPath())) {
            return userCase2Spec(serviceCase);
        }
        // others?
        return new ArrayList<>();
    }

    private List<MethodSpec> userCase2Spec(TestedMethodModel serviceCase) {
        String methodPath = serviceCase.getMethodPath();
        Set<UserCase> userCases = userCaseData.get(methodPath);
        SibylLog.info("handling: " + methodPath);
        int counter = 0;
        List<MethodSpec> ret = new ArrayList<>();

        UserCaseLoop:
        for (UserCase userCase : userCases) {
            MethodSpec.Builder methodBuilder =
                    MethodSpec.methodBuilder("test" + serviceCase.getMethodName() + (counter++))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(void.class);
            methodBuilder.addAnnotation(Test.class);

            List userParams = gson.fromJson(userCase.getRequest(), List.class);
            if (null == userParams) {
                userParams = new ArrayList<>();
            }
            List<Parameter> requiredParams = serviceCase.getParams();
            if (null == requiredParams) {
                requiredParams = new ArrayList<>();
            }

            // params check error, skip this case
            if (userParams.size() != requiredParams.size()) {
                continue;
            }

            for (int i = 0; i < userParams.size(); i++) {
                Parameter eachParam = requiredParams.get(i);
                Object paramData = userParams.get(i);

                String typeForGuess = CommonUtils.removeGenerics(eachParam.getType());

                ClassName guessed;
                try {
                    guessed = ClassName.bestGuess(typeForGuess);
                } catch (IllegalArgumentException e) {
                    // guess failed
                    guessed =
                            ClassName.get(
                                    CommonUtils.fullPath2PackageName(typeForGuess),
                                    CommonUtils.fullPath2ClazzName(typeForGuess));
                }

                if (null == paramData) {
                    // give up this method
                    continue UserCaseLoop;
                }
                methodBuilder.addStatement(
                        "$T $N = new $T().fromJson($S, $T.class)",
                        guessed,
                        eachParam.getName(),
                        Gson.class,
                        gson.toJson(paramData),
                        guessed);
            }

            // call
            String paramsStr = "";

            if (requiredParams.size() == 1) {
                paramsStr = requiredParams.get(0).getName();
            } else if (requiredParams.size() > 1) {
                paramsStr =
                        requiredParams.stream()
                                .filter(Objects::nonNull)
                                .map(Parameter::getName)
                                .collect(Collectors.joining(", "));
            }

            if (Objects.equals(serviceCase.getReturnType(), "void")) {
                // no return, directly end
                methodBuilder.addCode(
                        String.format(
                                "%s.%s(%s);\n",
                                CommonUtils.toLowerCaseForFirstLetter(
                                        serviceCase.getServiceClazzLiberalName()),
                                serviceCase.getMethodName(),
                                paramsStr));
            } else {
                // validate return value
                methodBuilder.addCode(
                        String.format(
                                "%s ret = %s.%s(%s);\n",
                                serviceCase.getReturnType(),
                                CommonUtils.toLowerCaseForFirstLetter(
                                        serviceCase.getServiceClazzLiberalName()),
                                serviceCase.getMethodName(),
                                paramsStr));
                methodBuilder.addCode(
                        CodeBlock.builder()
                                .addStatement("$T.out.println(ret)", System.class)
                                .build());

                methodBuilder.addCode(
                        CodeBlock.builder()
                                .addStatement("$T gson = new $T()", Gson.class, Gson.class)
                                .build());
                methodBuilder.addCode(
                        CodeBlock.builder()
                                .addStatement(
                                        "$T.assertEquals(gson.toJsonTree(ret), gson.fromJson($S,"
                                                + " $T.class))",
                                        Assert.class,
                                        userCase.getResponse(),
                                        JsonElement.class)
                                .build());
            }

            ret.add(methodBuilder.build());
        }
        return ret;
    }

    public List<MethodSpec> cases2Specs(Iterable<TestedMethodModel> serviceCases) {
        return StreamSupport.stream(serviceCases.spliterator(), false)
                .map(this::case2Spec)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public JUnitCaseFile specs2JavaFile(
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
        clazzBuilder.addAnnotation(SpringBootTest.class);

        TypeSpec cur = clazzBuilder.build();
        JavaFile jf =
                JavaFile.builder(packageName, cur)
                        .addFileComment("THIS FILE WAS AUTO-GENERATED BY MACHINE. DO NOT EDIT.")
                        .indent("    ")
                        .build();

        return JUnitCaseFile.of(jf);
    }

    public JUnitCaseFile cases2JavaFile(
            String packageName, String clazzName, Iterable<TestedMethodModel> serviceCases) {
        return specs2JavaFile(packageName, clazzName, cases2Specs(serviceCases));
    }

    public List<JUnitCaseFile> cases2JavaFiles(Iterable<TestedMethodModel> serviceCases) {
        List<JUnitCaseFile> ret = new ArrayList<>();
        Map<String, List<TestedMethodModel>> packageDimMap =
                StreamSupport.stream(serviceCases.spliterator(), false)
                        .collect(
                                Collectors.groupingBy(
                                        TestedMethodModel::getServicePackageName,
                                        Collectors.toList()));
        for (String packageName : packageDimMap.keySet()) {
            List<JUnitCaseFile> cur = cases2JavaFiles(packageName, packageDimMap.get(packageName));
            ret.addAll(cur);
        }
        return ret;
    }

    public List<JUnitCaseFile> cases2JavaFiles(
            String packageName, Iterable<TestedMethodModel> serviceCases) {
        List<JUnitCaseFile> ret = new ArrayList<>();
        Map<String, List<TestedMethodModel>> clazzDimMap =
                StreamSupport.stream(serviceCases.spliterator(), false)
                        .collect(
                                Collectors.groupingBy(
                                        TestedMethodModel::getServiceClazzName,
                                        Collectors.toList()));
        for (String clazzName : clazzDimMap.keySet()) {
            JUnitCaseFile cur = cases2JavaFile(packageName, clazzName, clazzDimMap.get(clazzName));
            ret.add(cur);
        }
        return ret;
    }
}
