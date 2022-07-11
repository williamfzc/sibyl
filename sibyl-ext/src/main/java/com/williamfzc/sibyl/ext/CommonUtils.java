package com.williamfzc.sibyl.ext;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CommonUtils {
    public static String toLowerCaseForFirstLetter(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
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
