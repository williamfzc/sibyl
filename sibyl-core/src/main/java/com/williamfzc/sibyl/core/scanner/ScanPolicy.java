package com.williamfzc.sibyl.core.scanner;

import java.io.File;

interface ScanPolicyHook {
    default boolean shouldExclude(File file) {
        return true;
    }
}

public class ScanPolicy implements ScanPolicyHook {
    public static ScanPolicy ofDefault() {
        return new ScanPolicy();
    }
}
