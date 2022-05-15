package com.williamfzc.sibyl.utgen;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.api.internal.SibylDiff;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.Test;

public class UtGen {
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
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(void.class);
            builder.addAnnotation(Test.class);

            // main execution
            try {
                builder.addCode(
                        String.format(
                                "%s o = new %s();\n",
                                method.getBelongsTo().getClazz().getFullName(),
                                method.getBelongsTo().getClazz().getFullName()));
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
                            TypeSpec cur =
                                    TypeSpec.classBuilder("Test" + item.getKey().getName())
                                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                            .addMethods(item.getValue())
                                            .build();
                            return JavaFile.builder(
                                            item.getKey().getBelongsTo().getPkg().getName(), cur)
                                    .build();
                        })
                .collect(Collectors.toSet());
    }
}
