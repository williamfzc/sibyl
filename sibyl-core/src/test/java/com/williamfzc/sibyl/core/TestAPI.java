package com.williamfzc.sibyl.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylDiff;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.Assert;
import org.junit.Test;

public class TestAPI {
    @Test
    public void testScan() throws IOException, InterruptedException {
        File src = Support.getSelfSource();
        Set<File> files = Sibyl.genValidFilesFromDir(src, SibylLangType.JAVA_8);
        SibylLog.info("valid files: " + files.size());
    }

    @Test
    public void testSnapshot() throws IOException, InterruptedException {
        File src = Support.getSelfSource();
        File target = Support.getWorkspace();
        File javaOutput = new File(target, "j8.json");

        Sibyl.genSnapshotFromDir(src, javaOutput, SibylLangType.JAVA_8);
        //        Sibyl.genSnapshotFromDir(src, new File(target, "kt.json"), SibylLangType.KOTLIN);

        // import and export
        File javaOutput2 = new File(target, "j8_2.json");
        Storage<Method> s = Storage.import_(javaOutput, Method.class);
        s.exportFile(javaOutput2);

        ObjectMapper mapper = new ObjectMapper();

        Assert.assertEquals(
                mapper.readTree(javaOutput).size(), mapper.readTree(javaOutput2).size());
    }

    @Test
    public void testCallGraph() throws IOException, InterruptedException {
        File src = Support.getSelfSource();
        File target = Support.getWorkspace();
        Sibyl.genCallGraphFromDir(src, new File(target, "j8_callgraph.json"), SibylLangType.JAVA_8);
    }

    @Test
    public void testDiff() throws IOException, InterruptedException {
        Repository repo = new RepositoryBuilder().findGitDir(Support.getProjectRoot()).build();
        ObjectId head = repo.resolve("HEAD");
        ObjectId headParent = repo.resolve("HEAD~~~~~");

        DiffResult diffResult = SibylDiff.diff(repo, head.getName(), headParent.getName());

        Storage<Method> methodStorage =
                Sibyl.genSnapshotFromDir(Support.getProjectRoot(), SibylLangType.JAVA_8);
        assert methodStorage != null;
        Set<DiffMethod> methods = Sibyl.genSnapshotDiff(methodStorage, diffResult);
        System.out.println("diff method count: " + methods.size());

        Map<String, List<DiffMethod>> output = new HashMap<>();
        methods.forEach(
                eachMethod -> {
                    String fileName = eachMethod.getBelongsTo().getFile().getName();
                    output.putIfAbsent(fileName, new LinkedList<>());
                    output.get(fileName).add(eachMethod);
                });

        output.forEach(
                (k, v) -> {
                    System.out.printf("file %s%n", k);
                    v.forEach(
                            eachMethod -> {
                                System.out.printf(
                                        "method: %s, score: %s, hit: %s%n",
                                        eachMethod.getInfo().getName(),
                                        eachMethod.calcDiffScore(),
                                        eachMethod.getDiffLines());
                            });
                });
    }
}
