package com.williamfzc.sibyl.utgen;

import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.Ctor;
import com.squareup.javapoet.*;
import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.api.internal.SibylDiff;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

public class UtGen {
    private static final String NAME_METHOD_TARGET_GETTER = "_getUtGenTarget";
    private static final String NAME_LITERAL_LOCAL_TARGET = "_utGenTarget";

    public static Storage<? extends Method> collectMethods(
            File projectDir, String endRev, String startRev)
            throws IOException, InterruptedException {
        Repository repo = new RepositoryBuilder().findGitDir(projectDir).build();
        ObjectId head = repo.resolve(endRev);
        ObjectId headParent = repo.resolve(startRev);
        SibylLog.info("after: " + head.getName());
        SibylLog.info("before: " + headParent.getName());

        DiffResult diffResult = SibylDiff.diff(repo, head.getName(), headParent.getName());

        ScanPolicy scanPolicy =
                new ScanPolicy() {
                    @Override
                    public boolean shouldExclude(File file) {
                        return diffResult.getNewFiles().stream()
                                .noneMatch(each -> file.getAbsolutePath().endsWith(each.getName()));
                    }
                };

        Storage<Method> methodStorage =
                Sibyl.genSnapshotFromDir(projectDir, SibylLangType.JAVA_8, scanPolicy);
        assert methodStorage != null;
        return Sibyl.genSnapshotDiff(methodStorage, diffResult);
    }

    public static Storage<? extends Method> collectAllMethods(File projectDir)
            throws IOException, InterruptedException {
        return Sibyl.genSnapshotFromDir(projectDir, SibylLangType.JAVA_8);
    }

    public static MethodSpec methodToFuzzCase(Method method) {
        // transfer this method
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder("test_" + method.getInfo().getName())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);
        methodBuilder.addAnnotation(Fuzz.class);

        // main execution
        try {
            AnnotationSpec ctorAnnotation =
                    AnnotationSpec.builder(From.class)
                            .addMember(
                                    "value",
                                    CodeBlock.builder().add("$T.class", Ctor.class).build())
                            .build();
            for (Parameter param : method.getInfo().getParams()) {
                // todo: bestGuess can not handle some primitive types (e.g. int
                methodBuilder.addParameter(
                        ParameterSpec.builder(ClassName.bestGuess(param.getType()), param.getName())
                                .addAnnotation(ctorAnnotation)
                                .build());
            }

            methodBuilder.addCode(
                    String.format(
                            "%s %s = (%s) %s();\n",
                            method.getBelongsTo().getClazz().getFullName(),
                            NAME_LITERAL_LOCAL_TARGET,
                            method.getBelongsTo().getClazz().getFullName(),
                            NAME_METHOD_TARGET_GETTER));
            // execute
            methodBuilder.addCode(
                    String.format(
                            "%s.%s(%s);\n",
                            NAME_LITERAL_LOCAL_TARGET,
                            method.getInfo().getName(),
                            method.getInfo().getParams().stream()
                                    .map(Parameter::getName)
                                    .collect(Collectors.joining(", "))));

        } catch (NullPointerException | IllegalArgumentException e) {
            return null;
        }
        return methodBuilder.build();
    }

    public static MethodSpec methodToUnitCase(Method method) {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder("test_" + method.getInfo().getName())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);

        // main execution
        try {
            methodBuilder.addCode(
                    String.format(
                            "%s %s = (%s) %s();\n",
                            method.getBelongsTo().getClazz().getFullName(),
                            NAME_LITERAL_LOCAL_TARGET,
                            method.getBelongsTo().getClazz().getFullName(),
                            NAME_METHOD_TARGET_GETTER));
            // execute
            // todo: specific mock / generator for params?
            methodBuilder.addCode(
                    String.format(
                            "%s.%s(%s);\n",
                            NAME_LITERAL_LOCAL_TARGET,
                            method.getInfo().getName(),
                            method.getInfo().getParams().stream()
                                    .map(Parameter::getName)
                                    .collect(Collectors.joining(", "))));

        } catch (NullPointerException | IllegalArgumentException e) {
            return null;
        }
        return methodBuilder.build();
    }

    public static Set<JavaFile> methodsToFuzzCases(Storage<? extends Method> methods) {
        return methodsToCases(methods, CaseType.FUZZ);
    }

    public static Set<JavaFile> methodsToUnitCases(Storage<? extends Method> methods) {
        return methodsToCases(methods, CaseType.UNIT);
    }

    public static Set<JavaFile> methodsToCases(
            Storage<? extends Method> methods, CaseType caseType) {
        Map<Clazz, List<MethodSpec>> cache = new HashMap<>();
        for (Method method : methods.getData()) {
            Clazz clazz = method.getBelongsTo().getClazz();

            MethodSpec methodSpec = null;
            switch (caseType) {
                case FUZZ:
                    methodSpec = methodToFuzzCase(method);
                    break;
                case UNIT:
                    methodSpec = methodToUnitCase(method);
                    break;
                default:
                    break;
            }
            if (null == methodSpec) {
                continue;
            }

            cache.putIfAbsent(clazz, new LinkedList<>());
            cache.get(clazz).add(methodSpec);
        }

        // create real cases
        return cache.entrySet().stream()
                .map(
                        item -> {
                            TypeSpec.Builder clazzBuilder =
                                    TypeSpec.classBuilder("Test" + item.getKey().getName())
                                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                            clazzBuilder.addMethods(item.getValue());
                            MethodSpec.Builder getterBuilder =
                                    MethodSpec.methodBuilder(NAME_METHOD_TARGET_GETTER)
                                            .addModifiers(Modifier.PRIVATE)
                                            .returns(Object.class);
                            getterBuilder.addCode(
                                    String.format(
                                            "return new %s();\n", item.getKey().getFullName()));
                            clazzBuilder.addMethod(getterBuilder.build());

                            AnnotationSpec.Builder runWithBuilder =
                                    AnnotationSpec.builder(RunWith.class);
                            switch (caseType) {
                                case FUZZ:
                                    runWithBuilder.addMember(
                                            "value",
                                            CodeBlock.builder().add("$T.class", JQF.class).build());

                                    break;
                                case UNIT:
                                    runWithBuilder =
                                            AnnotationSpec.builder(RunWith.class)
                                                    .addMember(
                                                            "value",
                                                            CodeBlock.builder()
                                                                    .add("$T.class", JUnit4.class)
                                                                    .build());
                                    break;
                                default:
                                    break;
                            }
                            clazzBuilder.addAnnotation(runWithBuilder.build());

                            TypeSpec cur = clazzBuilder.build();
                            return JavaFile.builder(
                                            item.getKey().getBelongsTo().getPkg().getName(), cur)
                                    .build();
                        })
                .collect(Collectors.toSet());
    }
}
