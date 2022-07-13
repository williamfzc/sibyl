package com.williamfzc.sibyl.ext.casegen.cli;

import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.ext.casegen.Processor;
import com.williamfzc.sibyl.ext.casegen.collector.SpringCollector;
import com.williamfzc.sibyl.ext.casegen.exporter.SpringJUnitExporter;
import com.williamfzc.sibyl.ext.casegen.model.JUnitCaseFile;
import com.williamfzc.sibyl.ext.casegen.model.TestedMethodModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

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

    @CommandLine.Option(
            names = {"-o", "--output"},
            required = true)
    private File outputDir;

    @CommandLine.Option(names = {"-a", "--assertEnabled"})
    private boolean assertEnabled = true;

    @CommandLine.Option(names = {"-ad", "--assertDefaultEnabled"})
    private boolean assertDefaultEnabled = true;

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

        Processor processor = new Processor();
        SpringCollector collector = new SpringCollector();
        try {
            Snapshot snapshot = collector.collectServices(srcDir);
            List<TestedMethodModel> models = processor.genTestedMethodModels(snapshot);
            SpringJUnitExporter exporter = new SpringJUnitExporter();
            exporter.setAssertEnabled(assertEnabled)
                    .setAssertDefaultEnabled(assertDefaultEnabled)
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
                                                            outputDir.toPath(), false);
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
}
