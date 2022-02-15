package com.williamfzc.sibyl.core.scanner;

import java.io.File;
import java.io.IOException;

// will not actually read file content
public class FileIntroScanner extends BaseFileScanner {
    public FileIntroScanner(File baseDir) {
        super(baseDir);
    }

    @Override
    public String getFileContent(File file) throws IOException {
        return "";
    }
}
