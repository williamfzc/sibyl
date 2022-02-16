package com.williamfzc.sibyl.cli;

import com.williamfzc.sibyl.cli.internal.DiffCommand;
import com.williamfzc.sibyl.cli.internal.SnapshotCommand;
import picocli.CommandLine;

// entry point
@CommandLine.Command(subcommands = {SnapshotCommand.class, DiffCommand.class})
public class SibylCli implements Runnable {
    public static void main(String[] args) {
        int ret = new CommandLine(new SibylCli()).execute(args);
        System.exit(ret);
    }

    @Override
    public void run() {}
}
