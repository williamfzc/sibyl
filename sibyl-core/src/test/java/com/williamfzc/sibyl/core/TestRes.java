package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class TestRes {
    @Test
    public void testGo() throws IOException, InterruptedException {
        File resourcesDirectory = new File("src/test/resources/go");
        Assert.assertTrue(resourcesDirectory.isDirectory());

        Snapshot snapshot = Sibyl.genSnapshotFromDir(resourcesDirectory, SibylLangType.GO);
        snapshot.getData().forEach(System.out::println);
    }

    @Test
    public void testKt() throws IOException, InterruptedException {
        File resourcesDirectory = new File("src/test/resources/kt");
        Assert.assertTrue(resourcesDirectory.isDirectory());
        Snapshot snapshot = Sibyl.genSnapshotFromDir(resourcesDirectory, SibylLangType.KOTLIN);
        snapshot.getData().forEach(System.out::println);
    }
}
