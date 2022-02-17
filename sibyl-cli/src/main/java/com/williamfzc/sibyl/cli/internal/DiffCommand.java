package com.williamfzc.sibyl.cli.internal;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylDiff;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

// todo: need test
@CommandLine.Command(name = "diff")
public class DiffCommand implements Runnable {

    @CommandLine.Option(
            names = {"-i", "--input"},
            required = true)
    private File input;

    @CommandLine.Option(
            names = {"-o", "--output"},
            required = true)
    private File output;

    @CommandLine.Option(
            names = {"--before"},
            required = true)
    private String before;

    @CommandLine.Option(
            names = {"--after"},
            required = true)
    private String after;

    @CommandLine.Option(
            names = {"-t", "--type"},
            required = true)
    private String langType;

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCommand.class);

    @Override
    public void run() {
        LOGGER.info(String.format("diff from %s to %s", before, after));
        try {
            DiffResult diffResult = SibylDiff.diff(input, after, before);
            Storage<Method> methodStorage =
                    Sibyl.genSnapshotFromDir(input, SibylLangType.valueOf(langType));
            assert methodStorage != null;
            Storage<DiffMethod> methods = Sibyl.genSnapshotDiff(methodStorage, diffResult);
            LOGGER.info("diff method count: " + methods.size());
            methods.exportFile(output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
