package com.williamfzc.sibyl.cli;

import com.williamfzc.sibyl.cli.internal.diff.DiffCommand;
import com.williamfzc.sibyl.cli.internal.snapshot.SnapshotCommand;
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
