package com.williamfzc.sibyl.core.scanner.file;

import java.io.File;

// will not actually read file content
public class FileIntroScanner extends BaseFileScanner {
    public FileIntroScanner(File baseDir) {
        super(baseDir);
    }

    @Override
    public String getFileContent(File file) {
        return "";
    }
}
