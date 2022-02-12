package com.williamfzc.sibyl.cli.internal;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "snapshot")
public class SnapshotCommand implements Runnable {
    @CommandLine.Option(
            names = {"-i", "--input"},
            required = true)
    private File input;

    @CommandLine.Option(
            names = {"-o", "--output"},
            required = true)
    private File output;

    @CommandLine.Option(
            names = {"-t", "--type"},
            required = true)
    private String langType;

    private static final Logger LOGGER = LoggerFactory.getLogger(SnapshotCommand.class);

    @Override
    public void run() {
        try {
            Sibyl.genSnapshotFromDir(input, output, SibylLangType.valueOf(langType));
            LOGGER.info("snapshot generated: " + output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
