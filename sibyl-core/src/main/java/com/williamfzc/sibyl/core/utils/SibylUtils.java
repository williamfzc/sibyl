package com.williamfzc.sibyl.core.utils;

public class SibylUtils {
    public static String getClazzNameFromPackageName(String packageName) {
        String[] parts = packageName.split("\\.");
        if (parts.length <= 1) {
            return packageName;
        }
        return parts[parts.length - 1];
    }
}
