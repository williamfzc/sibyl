package com.williamfzc.sibyl.cli.internal;

import com.williamfzc.sibyl.cli.CliLog;
import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import java.io.File;
import java.io.IOException;
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

    @Override
    public void run() {
        try {
            Sibyl.genSnapshotFromDir(input, output, SibylLangType.valueOf(langType));
            CliLog.info("snapshot generated: " + output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
