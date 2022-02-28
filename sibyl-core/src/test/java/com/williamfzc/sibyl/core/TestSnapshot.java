package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.test.Support;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.Test;

public class TestSnapshot {
    @Test
    public void testClazzQuery() throws IOException, InterruptedException {
        File src = Support.getSelfSource();

        Snapshot snapshot = Sibyl.genSnapshotFromDir(src, SibylLangType.JAVA_8);
        snapshot.listFileName()
                .forEach(
                        eachFileName -> {
                            System.out.println("file: " + eachFileName);
                        });

        snapshot.listClazzName()
                .forEach(
                        eachClazzName -> {
                            Set<Method> methods =
                                    snapshot.queryMethodsByFullClazzName(eachClazzName);
                            System.out.println(methods);
                        });
    }
}
