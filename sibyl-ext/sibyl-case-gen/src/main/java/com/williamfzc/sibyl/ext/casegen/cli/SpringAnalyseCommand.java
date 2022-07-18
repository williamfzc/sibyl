package com.williamfzc.sibyl.ext.casegen.cli;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.JUnitRunnerType;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.SpringJUnitExporter;
import com.williamfzc.sibyl.ext.casegen.model.junit.JUnitCaseFile;
import com.williamfzc.sibyl.ext.casegen.model.rt.TestedMethodModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@CommandLine.Command(name = "spring")
public class SpringAnalyseCommand implements Runnable {
    @CommandLine.Option(
            names = {"-c", "--case"},
            required = true)
    private File caseFile;

    @CommandLine.Option(
            names = {"-s", "--src"},
            required = true)
    private File srcDir;

    @CommandLine.Option(names = {"--exclude"})
    private String exclude = "";
    private static final String FLAG_SPLIT_EXCLUDE = ";";

    @CommandLine.Option(
            names = {"-o", "--output"},
            required = true)
    private File outputDir;

    @CommandLine.Option(names = {"-a", "--assertEnabled"})
    private boolean assertEnabled = true;

    @CommandLine.Option(names = {"-ad", "--assertDefaultEnabled"})
    private boolean assertDefaultEnabled = true;

    @CommandLine.Option(names = {"-rt", "--runnerType"})
    private JUnitRunnerType runnerType = JUnitRunnerType.MOCKITO;

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectCommand.class);

    @Override
    public void run() {
        try {
            validate();
            LOGGER.info("validate finished.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            Snapshot snapshot = genSnapshot();
            List<TestedMethodModel> models = TestedMethodModel.of(snapshot);
            SpringJUnitExporter exporter = new SpringJUnitExporter();
            exporter.setAssertEnabled(assertEnabled)
                    .setAssertDefaultEnabled(assertDefaultEnabled)
                    .setRunnerType(runnerType)
                    .importUserCases(caseFile);
            LOGGER.info("import user cases finished, count: " + exporter.getUserCaseData().size());
            List<JUnitCaseFile> javaFiles = exporter.models2JavaFiles(models);
            LOGGER.info("import method models finished, count: " + javaFiles.size());
            long successCount =
                    javaFiles.stream()
                            .filter(
                                    eachJavaFile -> {
                                        try {
                                            Path realPath =
                                                    eachJavaFile.writeToDir(
                                                            outputDir.toPath(), true);
                                            LOGGER.info("gen java file finished: " + realPath);
                                            return true;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            return false;
                                        }
                                    })
                            .count();
            LOGGER.info("generate finished, count: " + successCount);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void validate() throws FileNotFoundException {
        if (!caseFile.isFile()) {
            throw new FileNotFoundException("case file is not a file: " + caseFile);
        }
        if (!srcDir.isDirectory()) {
            throw new FileNotFoundException("srcDir is not a dir: " + srcDir);
        }
        // ok
    }

    private Snapshot genSnapshot() throws IOException, InterruptedException {
        if (exclude.isEmpty()) {
            return Sibyl.genSnapshotFromDir(srcDir, SibylLangType.JAVA_8);
        }
        String[] excludes = exclude.split(FLAG_SPLIT_EXCLUDE);

        return Sibyl.genSnapshotFromDir(srcDir, SibylLangType.JAVA_8, new ScanPolicy() {
            @Override
            public boolean shouldExclude(File file) {
                // wildcard
                String path = file.toPath().toString();
                return Arrays.stream(excludes).anyMatch(path::matches);
            }
        });
    }
}
