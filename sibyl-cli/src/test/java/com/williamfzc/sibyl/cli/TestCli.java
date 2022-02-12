package com.williamfzc.sibyl.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class TestCli {
    @Test
    public void testSnapshot() throws InterruptedException, IOException {
        File outputFile = new File(getTargetDir(), "a.json");
        File inputFile = getCurrentModule();

        ProcessBuilder pb = getSnapshotProcessBuilder(inputFile, outputFile);
        Process p = pb.start();
        boolean ret = p.waitFor(10, TimeUnit.SECONDS);

        Assert.assertTrue(ret);
        Assert.assertTrue(outputFile.isFile());
    }

    @Test
    public void testPerf() {
        Assume.assumeTrue(getTestRes().isDirectory());
        Arrays.stream(Objects.requireNonNull(getTestRes().listFiles()))
                .forEach(
                        inputFile -> {
                            System.out.println("testing perf: " + inputFile);
                            File outputFile =
                                    new File(getTargetDir(), inputFile.getName() + ".json");
                            ProcessBuilder pb = getSnapshotProcessBuilder(inputFile, outputFile);
                            try {
                                Process p = pb.start();
                                boolean ret = p.waitFor(180, TimeUnit.SECONDS);
                                Assert.assertTrue(ret);
                                Assert.assertTrue(outputFile.isFile());
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
    }

    private ProcessBuilder getSnapshotProcessBuilder(File input, File output) {
        File jarFile = getJar();
        Assert.assertTrue(getJar().isFile());
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

    private File getTestRes() {
        return new File(getRoot(), "testRes");
    }

    private File getRoot() {
        return getCurrentModule().getParentFile();
    }

    private File getCurrentModule() {
        Path currentRelativePath = Paths.get("");
        return new File(currentRelativePath.toAbsolutePath().toString());
    }

    private File getTargetDir() {
        return new File(getCurrentModule(), "target");
    }

    private File getJar() {
        String ver = System.getProperty("sibylVersion");
        return new File(
                getTargetDir(), String.format("sibyl-cli-%s-jar-with-dependencies.jar", ver));
    }

    private static final File NULL_FILE =
            new File((System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"));
}
