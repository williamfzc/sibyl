package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class TestAPI {
    Path currentRelativePath = Paths.get("");
    File src = new File(currentRelativePath.toAbsolutePath().toString(), "src");

    @Test
    public void testSnapshot() throws IOException, InterruptedException {
        Sibyl.genSnapshotFromDir(src, new File("j8.json"), SibylLangType.JAVA_8);
        Sibyl.genSnapshotFromDir(src, new File("kt.json"), SibylLangType.KOTLIN);
    }

    @Test
    public void testCallGraph() throws IOException, InterruptedException {
        Sibyl.genCallGraphFromDir(src, new File("j8_callgraph.json"), SibylLangType.JAVA_8);
    }
}
