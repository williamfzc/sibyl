package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylDiff;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.Test;

public class TestAPI {
    @Test
    public void testSnapshot() throws IOException, InterruptedException {
        File src = Support.getSelfSource();
        File target = Support.getWorkspace();
        Sibyl.genSnapshotFromDir(src, new File(target, "j8.json"), SibylLangType.JAVA_8);
        Sibyl.genSnapshotFromDir(src, new File(target, "kt.json"), SibylLangType.KOTLIN);
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
        ObjectId headParent = repo.resolve("HEAD^^^^^^");

        DiffResult diffResult = SibylDiff.diff(repo, head.getName(), headParent.getName());

        Storage<Method> methodStorage =
                Sibyl.genSnapshotFromDir(Support.getProjectRoot(), SibylLangType.JAVA_8);
        assert methodStorage != null;
        Set<Method> methods = Sibyl.genSnapshotDiff(methodStorage, diffResult);
        System.out.println("diff method count: " + methods.size());
        methods.forEach(eachMethod -> System.out.println(eachMethod.toString()));
    }
}
