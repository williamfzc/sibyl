package com.williamfzc.sibyl.ext.casegen.exporter.junit;

import com.google.gson.Gson;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.squareup.javapoet.*;
import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper.BaseWrapper;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper.GoblinWrapper;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper.MockitoWrapper;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.wrapper.SpringWrapper;
import com.williamfzc.sibyl.ext.casegen.model.RtObjectRepresentation;
import com.williamfzc.sibyl.ext.casegen.model.junit.JUnitCaseFile;
import com.williamfzc.sibyl.ext.casegen.model.rt.TestedMethodModel;
import com.williamfzc.sibyl.ext.casegen.model.rt.UserCase;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpringJUnitExporter extends JUnitExporter {
    // config
    private boolean assertEnabled = true;
    private boolean assertDefaultEnabled = true;
    private JUnitRunnerType runnerType = JUnitRunnerType.MOCKITO;

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

    public BaseWrapper getWrapper() {
        switch (runnerType) {
            // todo: singleton?
            case SPRING:
                return new SpringWrapper();
            case GOBLIN:
                return new GoblinWrapper();
            case MOCKITO:
                return new MockitoWrapper();
            default:
                return null;
        }
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
        getWrapper().wrapMethodAnnotation(methodBuilder);

        // always a json list
        List<RtObjectRepresentation> userParams = userCase.getRequest();
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
            RtObjectRepresentation paramData = userParams.get(i);

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
                        Consts.NAME_GSON,
                        paramData.getValidJsonValue(),
                        guessedWithGenerics);
            } else if (valueType.equals(RtObjectRepresentation.TYPE_VALUE_PROTOBUF)) {
                methodBuilder.addException(InvalidProtocolBufferException.class);
                String raw = paramData.getValidJsonValue();

                try {
                    String readable = JsonFormat.printer().print(Any.parseFrom(Base64.getDecoder().decode(raw)));
                    methodBuilder.addStatement(
                            "$T $N = $T.parser().merge($S, $T.builder())",
                            guessed,
                            eachParam.getName(),
                            guessed,
                            readable,
                            guessed
                    );
                } catch (InvalidProtocolBufferException e) {
                    // fallback: parse in runtime
                    SibylLog.warn("failed to use Any for parsing pb object, fallback");
                    methodBuilder.addStatement(
                            "$T $N = $T.parseFrom($T.getDecoder().decode($S))",
                            guessed,
                            eachParam.getName(),
                            guessed,
                            Base64.class,
                            paramData.getValidJsonValue()
                    );
                }
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
                                    "$T $N = $N.$N($N)",
                                    returnType,
                                    Consts.NAME_FIELD_ACTUAL,
                                    SibylUtils.toLowerCaseForFirstLetter(
                                            model.getClazzLiberalName()),
                                    model.getMethodName(),
                                    paramsStr)
                            .build());

            // assert
            if (assertEnabled) {
                TypeName clazz = parseClazzStr(userCase.getResponse().getType());
                if (userCase.getResponse().getValueType().equals(RtObjectRepresentation.TYPE_VALUE_PROTOBUF)) {
                    methodBuilder.addException(InvalidProtocolBufferException.class);
                    String raw = userCase.getResponse().getValidJsonValue();
                    try {
                        String readable = JsonFormat.printer().print(Any.parseFrom(Base64.getDecoder().decode(raw)));
                        methodBuilder.addStatement(
                                "$T $N = $T.parser().merge($S, $T.builder())",
                                clazz,
                                Consts.NAME_FIELD_EXPECT,
                                clazz,
                                readable,
                                clazz
                        );
                    } catch (InvalidProtocolBufferException e) {
                        // fallback: parse in runtime
                        SibylLog.warn("failed to use Any for parsing pb object, fallback");
                        methodBuilder.addStatement(
                                "$T $N = $T.parseFrom($T.getDecoder().decode($S))",
                                clazz,
                                Consts.NAME_FIELD_EXPECT,
                                clazz,
                                Base64.class,
                                raw
                                );
                    }
                } else {
                    methodBuilder.addStatement(
                            "$T $N = $N.fromJson($S, $T.class)",
                            clazz,
                            Consts.NAME_FIELD_EXPECT,
                            Consts.NAME_GSON,
                            userCase.getResponse().getValidJsonValue(),
                            clazz);
                }

                methodBuilder.addCode(
                        CodeBlock.builder()
                                .addStatement(
                                        "$N($S, $S)",
                                        Consts.NAME_JUDGE_HELPER, Consts.NAME_FIELD_ACTUAL, Consts.NAME_FIELD_EXPECT)
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
                TypeSpec.classBuilder(clazzName + "Tests")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // fields
        FieldSpec.Builder fieldBuilder = FieldSpec.builder(
                ClassName.get(packageName, clazzName),
                SibylUtils.toLowerCaseForFirstLetter(clazzName));
        getWrapper().wrapField(fieldBuilder);
        clazzBuilder.addField(fieldBuilder.build());
        clazzBuilder.addField(Gson.class, Consts.NAME_GSON);
        clazzBuilder.addInitializerBlock(
                CodeBlock.builder().addStatement("$N = new $T()", Consts.NAME_GSON, Gson.class).build());

        // methods
        clazzBuilder.addMethods(methodSpecs);
        if (clazzBuilder.methodSpecs.isEmpty()) {
            return null;
        }
        getWrapper().wrapClazzAnnotation(clazzBuilder);

        clazzBuilder.addMethod(createJudgeHelper());
        TypeSpec cur = clazzBuilder.build();
        JavaFile jf =
                JavaFile.builder(packageName, cur)
                        .addFileComment("")
                        .addFileComment("This file was auto-generated.")
                        .addFileComment("")
                        .indent("    ")
                        .build();

        return JUnitCaseFile.of(jf);
    }

    private MethodSpec createJudgeHelper() {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder(Consts.NAME_JUDGE_HELPER)
                        .addModifiers(Modifier.PRIVATE)
                        .returns(void.class);
        methodBuilder.addParameter(Object.class, Consts.NAME_FIELD_ACTUAL);
        methodBuilder.addParameter(Object.class, Consts.NAME_FIELD_EXPECT);
        methodBuilder.addComment("You can add your own assertions here, such as json compare.");
        methodBuilder.addCode(
                CodeBlock.builder().addStatement("$T.out.println($N)", System.class, Consts.NAME_FIELD_ACTUAL).build());
        methodBuilder.addCode(
                CodeBlock.builder().addStatement("$T.out.println($N)", System.class, Consts.NAME_FIELD_EXPECT).build());


        if (!assertDefaultEnabled) {
            methodBuilder.addStatement("return true");
            return methodBuilder.build();
        }
        // default compare
        getWrapper().wrapJudgeMethod(methodBuilder);
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
