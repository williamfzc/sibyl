package com.williamfzc.sibyl.ext.casegen.cli;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(name = "collect")
public class CollectCommand implements Runnable {
    @CommandLine.Option(names = {"-d", "--dir"})
    private File srcDir;

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectCommand.class);

    @Override
    public void run() {
        LOGGER.info("src: {}", srcDir);
    }
}
