package com.williamfzc.sibyl.ext.casegen;

import com.williamfzc.sibyl.ext.casegen.cli.CollectCommand;
import picocli.CommandLine;

@CommandLine.Command(subcommands = {CollectCommand.class})
public class CaseGenCli implements Runnable {
    public static void main(String[] args) {
        int ret = new CommandLine(new CaseGenCli()).execute(args);
        System.exit(ret);
    }

    @Override
    public void run() {
    }
}


