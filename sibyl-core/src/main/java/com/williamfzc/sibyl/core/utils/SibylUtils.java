package com.williamfzc.sibyl.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SibylUtils {
    // com.abc.def.A -> A
    public static String getClazzNameFromPackageName(String packageName) {
        return fullPath2ClazzName(packageName);
    }

    // convert path string format to the current system's one
    public static String formatPath(String path) {
        return Paths.get(path).toString();
    }

    // read file content, java8
    public static String readContent(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static String fileRelative(File file1, File file2) {
        // https://stackoverflow.com/a/205655/10641498
        return file1.toPath().relativize(file2.toPath()).toString();
    }

    public static String toLowerCaseForFirstLetter(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String toUpperCaseForFirstLetter(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String fullPath2ClazzName(String fullPath) {
        if (!fullPath.contains(".")) {
            return fullPath;
        }
        return fullPath.substring(fullPath.lastIndexOf('.') + 1);
    }

    public static String fullPath2PackageName(String fullPath) {
        if (!fullPath.contains(".")) {
            return "";
        }
        return fullPath.substring(0, fullPath.lastIndexOf('.'));
    }

    public static String removeGenerics(String clazz) {
        if (!(clazz.contains("<") && clazz.contains(">"))) {
            return clazz;
        }

        return Arrays.stream(clazz.split("<")).collect(Collectors.toList()).get(0);
    }
}
