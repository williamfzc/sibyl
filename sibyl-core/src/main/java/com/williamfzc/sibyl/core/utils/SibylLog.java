package com.williamfzc.sibyl.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SibylLog {
    private static final Logger LOGGER = LoggerFactory.getLogger(SibylLog.class);

    public static void debug(String msg) {
        LOGGER.debug("[sibyl-D] " + msg);
    }

    public static void info(String msg) {
        LOGGER.info("[sibyl-I] " + msg);
    }

    public static void warn(String msg) {
        LOGGER.warn("[sibyl-W] " + msg);
    }

    public static void error(String msg) {
        LOGGER.error("[sibyl-E] " + msg);
    }
}
