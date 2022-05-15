package com.williamfzc.sibyl.utgen;

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

    public static Set<JavaFile> methodsToCases(Storage<? extends Method> methods) {
        Map<Clazz, List<MethodSpec>> cache = new HashMap<>();
        for (Method method : methods.getData()) {
            Clazz clazz = method.getBelongsTo().getClazz();

            // transfer this method
            MethodSpec.Builder builder =
                    MethodSpec.methodBuilder("test_" + method.getInfo().getName())
                            .addModifiers(Modifier.PUBLIC)
                            .returns(void.class);
            builder.addAnnotation(Fuzz.class);

            // main execution
            try {
                builder.addCode(
                        String.format(
                                "%s %s = %s();\n",
                                method.getBelongsTo().getClazz().getFullName(),
                                NAME_LITERAL_LOCAL_TARGET,
                                NAME_METHOD_TARGET_GETTER));
                // execute
                for (Parameter param : method.getInfo().getParams()) {
                    builder.addParameter(ClassName.bestGuess(param.getType()), param.getName());
                }
                builder.addCode(
                        String.format(
                                "%s.%s(%s);\n",
                                NAME_LITERAL_LOCAL_TARGET,
                                method.getInfo().getName(),
                                method.getInfo().getParams().stream()
                                        .map(Parameter::getName)
                                        .collect(Collectors.joining(", "))));

            } catch (NullPointerException e) {
                continue;
            }

            cache.putIfAbsent(clazz, new LinkedList<>());
            cache.get(clazz).add(builder.build());
        }

        // create real cases
        return cache.entrySet().stream()
                .map(
                        item -> {
                            TypeSpec.Builder builder =
                                    TypeSpec.classBuilder("Test" + item.getKey().getName())
                                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                            builder.addMethods(item.getValue());
                            MethodSpec.Builder getterBuilder =
                                    MethodSpec.methodBuilder(NAME_METHOD_TARGET_GETTER)
                                            .addModifiers(Modifier.PRIVATE)
                                            .returns(
                                                    ClassName.get(
                                                            item.getKey()
                                                                    .getBelongsTo()
                                                                    .getPkg()
                                                                    .getName(),
                                                            item.getKey().getName()));
                            getterBuilder.addCode(
                                    String.format("return new %s();\n", item.getKey().getName()));
                            builder.addMethod(getterBuilder.build());
                            AnnotationSpec.Builder runWithBuilder =
                                    AnnotationSpec.builder(RunWith.class)
                                            .addMember(
                                                    "value",
                                                    CodeBlock.builder()
                                                            .add("$T.class", JQF.class)
                                                            .build());
                            builder.addAnnotation(runWithBuilder.build());

                            TypeSpec cur = builder.build();
                            return JavaFile.builder(
                                            item.getKey().getBelongsTo().getPkg().getName(), cur)
                                    .build();
                        })
                .collect(Collectors.toSet());
    }
}
