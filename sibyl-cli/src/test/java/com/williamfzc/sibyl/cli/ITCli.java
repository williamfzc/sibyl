package com.williamfzc.sibyl.cli;

import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.test.Support;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class ITCli {
    @Test
    public void testSnapshot() throws InterruptedException, IOException {
        File outputFile = new File(Support.getTargetDir(), "a.json");
        File inputFile = Support.getModuleRoot();

        ProcessBuilder pb = getSnapshotProcessBuilder(inputFile, outputFile);
        Process p = pb.start();
        boolean ret = p.waitFor(10, TimeUnit.SECONDS);

        Assert.assertTrue(ret);
        Assert.assertTrue(outputFile.isFile());
    }

    @Test
    public void testPerf() {
        Assume.assumeTrue(Support.getTestRes().isDirectory());
        File[] testResList = Support.getTestRes().listFiles();
        Assume.assumeNotNull((Object) testResList);
        for (File inputFile : testResList) {
            SibylLog.info("testing perf: " + inputFile);
            File outputFile = new File(Support.getTargetDir(), inputFile.getName() + ".json");
            ProcessBuilder pb = getSnapshotProcessBuilder(inputFile, outputFile);
            Process p = null;
            try {
                p = pb.start();
                boolean ret = p.waitFor(1800, TimeUnit.SECONDS);
                Assert.assertTrue(ret);
                Assert.assertTrue(outputFile.isFile());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (null != p && p.isAlive()) {
                    p.destroy();
                }
            }
        }
    }

    private ProcessBuilder getSnapshotProcessBuilder(File input, File output) {
        File jarFile = Support.getJar();
        Assert.assertTrue(Support.getJar().isFile());
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(
                "java",
                "-jar",
                jarFile.getAbsolutePath(),
                "snapshot",
                "-i",
                input.getAbsolutePath(),
                "-o",
                output.getAbsolutePath(),
                "-t",
                "JAVA_8");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        System.out.println(String.join(" ", pb.command()));
        return pb;
    }
}
