package com.williamfzc.sibyl.cli.internal;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.api.internal.SibylDiff;
import com.williamfzc.sibyl.core.model.diff.DiffFile;
import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
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

    @CommandLine.Option(names = {"-g", "--git"})
    private File gitDir;

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffCommand.class);

    @Override
    public void run() {
        LOGGER.info(String.format("diff from %s to %s", before, after));
        try {
            // by default use input as git dir
            if (null == gitDir) {
                gitDir = input;
            }
            DiffResult diffResult = SibylDiff.diff(gitDir, after, before);
            List<DiffFile> files = diffResult.getNewFiles();

            ScanPolicy scanPolicy =
                    new ScanPolicy() {
                        @Override
                        public boolean shouldExclude(File file) {
                            return files.stream()
                                    .noneMatch(
                                            each ->
                                                    file.getAbsolutePath()
                                                            .endsWith(each.getName()));
                        }
                    };
            Storage<Method> methodStorage =
                    Sibyl.genSnapshotFromDir(input, SibylLangType.valueOf(langType), scanPolicy);

            assert methodStorage != null;
            Storage<DiffMethod> methods =
                    Sibyl.genSnapshotDiff(
                            methodStorage, diffResult, SibylUtils.fileRelative(gitDir, input));
            LOGGER.info("diff method count: " + methods.size());
            methods.exportFile(output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
