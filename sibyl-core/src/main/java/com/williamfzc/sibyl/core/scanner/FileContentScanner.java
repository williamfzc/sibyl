package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.utils.SibylUtils;
import java.io.File;
import java.io.IOException;

public class FileContentScanner extends BaseFileScanner {
    public FileContentScanner(File baseDir) {
        super(baseDir);
    }

    @Override
    public String getFileContent(File file) throws IOException {
        return SibylUtils.readContent(file);
    }
}
