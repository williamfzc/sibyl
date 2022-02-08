package com.williamfzc.sibyl.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "hello", description = "Says hello")
public class SibylCli implements Runnable {
    public static void main(String[] args) {
        int ret = new CommandLine(new SibylCli()).execute(args);
        System.exit(ret);
    }

    @Override
    public void run() {
        System.out.println("Hello World!");
    }
}
