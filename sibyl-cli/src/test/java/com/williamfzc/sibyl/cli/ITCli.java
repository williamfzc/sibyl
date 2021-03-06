package com.williamfzc.sibyl.cli;

import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.storage.snapshot.DiffSnapshot;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.test.Support;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
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
        boolean ret = p.waitFor(1, TimeUnit.MINUTES);

        Assert.assertTrue(ret);
        Assert.assertTrue(outputFile.isFile());
    }

    @Test
    public void testSnapshotPerf() {
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

    @Test
    public void testDiff() throws IOException, InterruptedException {
        File outputFile = new File(Support.getTargetDir(), "b.json");
        File inputFile = Support.getCoreRoot();

        ProcessBuilder pb = getDiffProcessBuilder(inputFile, outputFile);
        Process p = pb.start();
        boolean ret = p.waitFor(10, TimeUnit.MINUTES);

        Assert.assertTrue(ret);
        Assert.assertTrue(outputFile.isFile());

        // display file
        DiffSnapshot methods = DiffSnapshot.initFrom(outputFile);
        Map<String, List<DiffMethod>> fileMap = new HashMap<>();
        methods.getData()
                .forEach(
                        eachMethod -> {
                            String fileName = eachMethod.getBelongsTo().getFile().getName();
                            fileMap.putIfAbsent(fileName, new LinkedList<>());
                            fileMap.get(fileName).add(eachMethod);
                        });

        fileMap.forEach(
                (k, v) -> {
                    System.out.println("file changed: " + k);
                    v.stream()
                            .sorted(Comparator.comparing(each -> -each.calcDiffScore()))
                            .forEachOrdered(
                                    each -> {
                                        System.out.printf(
                                                "method %s score %s%n",
                                                each.getInfo().getName(), each.calcDiffScore());
                                    });
                });
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

    private ProcessBuilder getDiffProcessBuilder(File input, File output) throws IOException {
        File jarFile = Support.getJar();
        Assert.assertTrue(jarFile.isFile());
        Repository repo = new RepositoryBuilder().findGitDir(Support.getProjectRoot()).build();
        ObjectId head = repo.resolve("HEAD");
        ObjectId headParent = repo.resolve("HEAD~~~~~");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(
                "java",
                "-jar",
                jarFile.getAbsolutePath(),
                "diff",
                "-i",
                input.getAbsolutePath(),
                "-o",
                output.getAbsolutePath(),
                "--before",
                head.getName(),
                "--after",
                headParent.getName(),
                "--git",
                Support.getProjectRoot().getAbsolutePath(),
                "-t",
                "JAVA_8");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        System.out.println(String.join(" ", pb.command()));
        return pb;
    }
}
