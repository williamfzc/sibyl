package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import java.io.File;
import java.io.IOException;
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
}
