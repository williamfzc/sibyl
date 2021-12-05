package com.williamfzc.sibyl.core.utils;

public class Log {
    public static void info(String msg) {
        System.out.println("[sibyl-I] " + msg);
    }

    public static void warn(String msg) {
        System.out.println("[sibyl-W] " + msg);
    }

    public static void error(String msg) {
        System.out.println("[sibyl-E] " + msg);
    }
}
