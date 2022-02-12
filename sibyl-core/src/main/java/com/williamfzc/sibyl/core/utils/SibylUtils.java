package com.williamfzc.sibyl.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SibylUtils {
    // com.abc.def.A -> A
    public static String getClazzNameFromPackageName(String packageName) {
        String[] parts = packageName.split("\\.");
        if (parts.length <= 1) {
            return packageName;
        }
        return parts[parts.length - 1];
    }

    // convert path string format to the current system's one
    public static String formatPath(String path) {
        return Paths.get(path).toString();
    }

    // read file content, java8
    public static String readContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
