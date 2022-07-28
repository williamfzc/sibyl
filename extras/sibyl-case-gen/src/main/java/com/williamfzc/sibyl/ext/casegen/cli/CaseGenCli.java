package com.williamfzc.sibyl.ext.casegen.cli;

import picocli.CommandLine;

@CommandLine.Command(subcommands = {GenCommand.class, CheckCommand.class})
public class CaseGenCli implements Runnable {
    public static void main(String[] args) {
        int ret = new CommandLine(new CaseGenCli()).execute(args);
        System.exit(ret);
    }

    @Override
    public void run() {}
}
