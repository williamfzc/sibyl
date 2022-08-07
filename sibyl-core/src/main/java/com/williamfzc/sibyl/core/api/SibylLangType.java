package com.williamfzc.sibyl.core.api;

public enum SibylLangType {
    JAVA_8(".java"),
    KOTLIN(".kt"),
    GO(".go");

    final String FILE_SUBFIX;

    SibylLangType(String fileSubfix) {
        this.FILE_SUBFIX = fileSubfix;
    }
}
