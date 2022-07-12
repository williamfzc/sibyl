package com.williamfzc.sibyl.ext.casegen.exporter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.squareup.javapoet.*;
import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import com.williamfzc.sibyl.ext.casegen.model.JUnitCaseFile;
import com.williamfzc.sibyl.ext.casegen.model.TestedMethodModel;
import com.williamfzc.sibyl.ext.casegen.model.UserCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpringJUnitExporter extends BaseExporter {
    // create methods
    // create fields
    // create test class
    private final Gson gson = new Gson();

    public List<MethodSpec> model2Spec(TestedMethodModel model) {
        if (userCaseData.containsKey(model.getMethodPath())) {
            return userCase2Spec(model);
        }
        // others?
        return new ArrayList<>();
    }

    private List<MethodSpec> userCase2Spec(TestedMethodModel model) {
        String methodPath = model.getMethodPath();
        Set<UserCase> userCases = userCaseData.get(methodPath);
        SibylLog.info("handling: " + methodPath);
        int counter = 0;
        List<MethodSpec> ret = new ArrayList<>();

        UserCaseLoop:
        for (UserCase userCase : userCases) {
            MethodSpec.Builder methodBuilder =
                    MethodSpec.methodBuilder("test" + SibylUtils.toUpperCaseForFirstLetter(model.getMethodName()) + (counter++))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(void.class);
            methodBuilder.addAnnotation(Test.class);

            List userParams = gson.fromJson(userCase.getRequest(), List.class);
            if (null == userParams) {
                userParams = new ArrayList<>();
            }
            List<Parameter> requiredParams = model.getParams();
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

                String typeForGuess = eachParam.getType();
                TypeName guessed = parseClazzStr(typeForGuess);

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

            if (Objects.equals(model.getReturnType(), "void")) {
                // no return, directly end
                methodBuilder.addCode(
                        String.format(
                                "%s.%s(%s);\n",
                                SibylUtils.toLowerCaseForFirstLetter(
                                        model.getClazzLiberalName()),
                                model.getMethodName(),
                                paramsStr));
            } else {
                TypeName returnType = parseClazzStr(model.getReturnType());

                // validate return value
                methodBuilder.addCode(
                        CodeBlock.builder().addStatement("$T ret = $N.$N($N)", returnType, SibylUtils.toLowerCaseForFirstLetter(model.getClazzLiberalName()), model.getMethodName(), paramsStr).build());
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

    private TypeName parseClazzStr(String clazzStr) {
        TypeName guessed;
        if (SibylUtils.isGenerics(clazzStr)) {
            // https://github.com/square/javapoet/discussions/921#discussioncomment-3125699
            ClassName rawType = ClassName.bestGuess(SibylUtils.generics2raw(clazzStr));
            String params = SibylUtils.generics2Param(clazzStr);
            if (params.contains(",")) {
                // more than one
                guessed = ParameterizedTypeName.get(
                        rawType,
                        Arrays.stream(params.split(","))
                                .map(String::trim)
                                .map(this::parseClazzStr)
                                .toArray(TypeName[]::new));
            } else {
                TypeName paramType = parseClazzStr(params);
                guessed = ParameterizedTypeName.get(rawType, paramType);
            }
        } else {
            try {
                guessed = ClassName.bestGuess(clazzStr);
            } catch (IllegalArgumentException e) {
                // guess failed
                guessed =
                        ClassName.get(
                                SibylUtils.fullPath2PackageName(clazzStr),
                                SibylUtils.fullPath2ClazzName(clazzStr));
            }
        }
        return guessed;
    }

    public List<MethodSpec> models2Specs(Iterable<TestedMethodModel> models) {
        return StreamSupport.stream(models.spliterator(), false)
                .map(this::model2Spec)
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
                                SibylUtils.toLowerCaseForFirstLetter(clazzName))
                        .addAnnotation(Resource.class)
                        .build());

        // methods
        clazzBuilder.addMethods(methodSpecs);
        if (clazzBuilder.methodSpecs.isEmpty()) {
            return null;
        }

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
                        .addFileComment("")
                        .addFileComment("This file was auto-generated.")
                        .addFileComment("Powered by sibyl project.")
                        .addFileComment("")
                        .indent("    ")
                        .build();

        return JUnitCaseFile.of(jf);
    }

    public JUnitCaseFile models2JavaFile(
            String packageName, String clazzName, Iterable<TestedMethodModel> models) {
        return specs2JavaFile(packageName, clazzName, models2Specs(models));
    }

    public List<JUnitCaseFile> models2JavaFiles(Iterable<TestedMethodModel> models) {
        List<JUnitCaseFile> ret = new ArrayList<>();
        Map<String, List<TestedMethodModel>> packageDimMap =
                StreamSupport.stream(models.spliterator(), false)
                        .collect(
                                Collectors.groupingBy(
                                        TestedMethodModel::getPackageName,
                                        Collectors.toList()));
        for (String packageName : packageDimMap.keySet()) {
            List<JUnitCaseFile> cur = models2JavaFiles(packageName, packageDimMap.get(packageName));
            ret.addAll(cur);
        }
        return ret;
    }

    public List<JUnitCaseFile> models2JavaFiles(
            String packageName, Iterable<TestedMethodModel> models) {
        List<JUnitCaseFile> ret = new ArrayList<>();
        Map<String, List<TestedMethodModel>> clazzDimMap =
                StreamSupport.stream(models.spliterator(), false)
                        .collect(
                                Collectors.groupingBy(
                                        TestedMethodModel::getClazzName,
                                        Collectors.toList()));
        for (String clazzName : clazzDimMap.keySet()) {
            JUnitCaseFile cur = models2JavaFile(packageName, clazzName, clazzDimMap.get(clazzName));
            if (null != cur) {
                ret.add(cur);
            }
        }
        return ret;
    }
}
