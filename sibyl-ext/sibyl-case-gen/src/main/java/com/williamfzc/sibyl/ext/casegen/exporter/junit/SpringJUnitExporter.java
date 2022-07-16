package com.williamfzc.sibyl.ext.casegen.exporter.junit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.protobuf.util.JsonFormat;
import com.squareup.javapoet.*;
import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import com.williamfzc.sibyl.ext.casegen.exporter.BaseExporter;
import com.williamfzc.sibyl.ext.casegen.model.junit.JUnitCaseFile;
import com.williamfzc.sibyl.ext.casegen.model.RtObjectRepresentation;
import com.williamfzc.sibyl.ext.casegen.model.rt.TestedMethodModel;
import com.williamfzc.sibyl.ext.casegen.model.rt.UserCase;

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

public class SpringJUnitExporter extends JUnitExporter {
    private final Gson gson = new Gson();
    private static final String NAME_JUDGE_HELPER = "_judgeHelper";
    private static final String NAME_GSON = "gson";

    // config
    private boolean assertEnabled = true;
    private boolean assertDefaultEnabled = true;
    private JUnitRunnerType runnerType = JUnitRunnerType.SPRING;

    public SpringJUnitExporter setAssertEnabled(boolean status) {
        assertEnabled = status;
        return this;
    }

    public SpringJUnitExporter setRunnerType(JUnitRunnerType type) {
        runnerType = type;
        return this;
    }

    public SpringJUnitExporter setAssertDefaultEnabled(boolean status) {
        assertDefaultEnabled = status;
        return this;
    }

    public List<MethodSpec> model2Spec(TestedMethodModel model) {
        if (userCaseData.containsKey(model.getMethodPath())) {
            return userCases2Spec(model);
        }
        // others?
        return new ArrayList<>();
    }

    private List<MethodSpec> userCases2Spec(TestedMethodModel model) {
        String methodPath = model.getMethodPath();
        Set<UserCase> userCases = userCaseData.get(methodPath);
        SibylLog.info("handling: " + methodPath);
        int counter = 0;
        List<MethodSpec> ret = new ArrayList<>();

        for (UserCase userCase : userCases) {
            MethodSpec methodSpec = userCase2Spec(userCase, model, counter);
            if (null != methodSpec) {
                ret.add(methodSpec);
                counter++;
            }
        }
        return ret;
    }

    private MethodSpec userCase2Spec(UserCase userCase, TestedMethodModel model, int counter) {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(
                                "test"
                                        + SibylUtils.toUpperCaseForFirstLetter(
                                        model.getMethodName())
                                        + "_"
                                        + counter)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);
        methodBuilder.addAnnotation(Test.class);

        // always a json list
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
            return null;
        }

        for (int i = 0; i < userParams.size(); i++) {
            Parameter eachParam = requiredParams.get(i);
            RtObjectRepresentation paramData = gson.fromJson(userParams.get(i).toString(), RtObjectRepresentation.class);

            String typeForGuess = eachParam.getType();
            TypeName guessed = parseClazzStr(typeForGuess);
            String guessedWithGenerics = SibylUtils.removeGenerics(guessed.toString());

            if (null == paramData) {
                // todo: when?
                // give up this method
                return null;
            }
            String valueType = paramData.getValueType();
            if (valueType.equals(RtObjectRepresentation.TYPE_VALUE_JSON)) {
                methodBuilder.addStatement(
                        "$T $N = $N.fromJson($S, $N.class)",
                        guessed,
                        eachParam.getName(),
                        NAME_GSON,
                        gson.toJson(paramData.getValue()),
                        guessedWithGenerics);
            } else if (valueType.equals(RtObjectRepresentation.TYPE_VALUE_PROTOBUF)) {
                methodBuilder.addStatement(
                        "$T $N = $T.parser().merge($S, $T.newBuilder())",
                        guessed,
                        eachParam.getName(),
                        JsonFormat.class,
                        paramData.getValue(),
                        guessed
                );
            } else {
                // give up
                return null;
            }
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
                            SibylUtils.toLowerCaseForFirstLetter(model.getClazzLiberalName()),
                            model.getMethodName(),
                            paramsStr));
        } else {
            TypeName returnType = parseClazzStr(model.getReturnType());

            // validate return value
            methodBuilder.addCode(
                    CodeBlock.builder()
                            .addStatement(
                                    "$T ret = $N.$N($N)",
                                    returnType,
                                    SibylUtils.toLowerCaseForFirstLetter(
                                            model.getClazzLiberalName()),
                                    model.getMethodName(),
                                    paramsStr)
                            .build());
            methodBuilder.addCode(
                    CodeBlock.builder().addStatement("$T.out.println(ret)", System.class).build());

            // assert
            if (assertEnabled) {
                methodBuilder.addStatement(
                        "$T actual = $N.toJsonTree(ret)", JsonElement.class, NAME_GSON);
                methodBuilder.addStatement(
                        "$T expect = $N.fromJson($S, $T.class)",
                        JsonElement.class,
                        NAME_GSON,
                        userCase.getResponse(),
                        JsonElement.class);

                methodBuilder.addCode(
                        CodeBlock.builder()
                                .addStatement(
                                        "$T.assertTrue($N(actual, expect))",
                                        Assert.class,
                                        NAME_JUDGE_HELPER)
                                .build());
            }
        }
        return methodBuilder.build();
    }

    private TypeName parseClazzStr(String clazzStr) {
        TypeName guessed;
        if (SibylUtils.isGenerics(clazzStr)) {
            // https://github.com/square/javapoet/discussions/921#discussioncomment-3125699
            ClassName rawType = ClassName.bestGuess(SibylUtils.generics2raw(clazzStr));
            String params = SibylUtils.generics2Param(clazzStr);
            if (params.contains(",")) {
                // more than one
                guessed =
                        ParameterizedTypeName.get(
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
        clazzBuilder.addField(Gson.class, NAME_GSON);
        clazzBuilder.addInitializerBlock(
                CodeBlock.builder().addStatement("$N = new $T()", NAME_GSON, Gson.class).build());

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
        clazzBuilder.addMethod(createJudgeHelper());
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

    private MethodSpec createJudgeHelper() {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(NAME_JUDGE_HELPER)
                        .addModifiers(Modifier.PRIVATE)
                        .returns(boolean.class);
        methodBuilder.addParameter(JsonElement.class, "actual");
        methodBuilder.addParameter(JsonElement.class, "expect");
        methodBuilder.addComment("You can add your own assertions here, such as json compare.");
        if (!assertDefaultEnabled) {
            methodBuilder.addStatement("return true");
            return methodBuilder.build();
        }
        // default compare
        methodBuilder.addStatement("return obj1.equals(obj2)");
        return methodBuilder.build();
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
                                        TestedMethodModel::getPackageName, Collectors.toList()));
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
                                        TestedMethodModel::getClazzName, Collectors.toList()));
        for (String clazzName : clazzDimMap.keySet()) {
            JUnitCaseFile cur = models2JavaFile(packageName, clazzName, clazzDimMap.get(clazzName));
            if (null != cur) {
                ret.add(cur);
            }
        }
        return ret;
    }
}
