package com.williamfzc.sibyl.core.scanner;

import java.io.File;

interface ScanPolicyHook {
    default boolean shouldExclude(File file) {
        return false;
    }
}

public class ScanPolicy implements ScanPolicyHook {
    public int threadPoolSize = Runtime.getRuntime().availableProcessors() - 1;

    public static ScanPolicy ofDefault() {
        return new ScanPolicy();
    }
}
