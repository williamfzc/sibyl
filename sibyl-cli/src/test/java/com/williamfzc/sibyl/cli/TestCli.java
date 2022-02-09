package com.williamfzc.sibyl.cli;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import picocli.CommandLine;

public class TestCli {
    @Test
    public void testSnapshot() {
        Path currentRelativePath = Paths.get("");
        File cur = new File(currentRelativePath.toAbsolutePath().toString());
        int ret =
                new CommandLine(new SibylCli())
                        .execute(
                                "snapshot",
                                "-i",
                                cur.getAbsolutePath(),
                                "-o",
                                new File(cur, "a.json").getAbsolutePath(),
                                "-t",
                                "JAVA_8");
        assertEquals(0, ret);
    }
}
