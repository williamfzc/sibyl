package com.williamfzc.sibyl.ext.casegen.cli;

import com.williamfzc.sibyl.ext.casegen.exporter.junit.SpringJUnitExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

@CommandLine.Command(name = "check")
public class CheckCommand implements Runnable {
    @CommandLine.Option(
            names = {"-c", "--case"},
            required = true)
    private File caseFile;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenCommand.class);

    @Override
    public void run() {
        if (!caseFile.isFile()) {
            LOGGER.error("case file is not a file: " + caseFile);
            return;
        }

        SpringJUnitExporter exporter = new SpringJUnitExporter();
        try {
            exporter.importUserCases(caseFile);
            LOGGER.info("import user cases finished, count: " + exporter.getUserCaseData().size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
