package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.utils.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class SimpleScanner extends BaseScanner {
    public boolean fileValid(File file) {
        // by default
        return true;
    }

    public void scanDir(String dirPath) throws IOException {
        Files.walkFileTree(Paths.get(dirPath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File fo = file.toFile();
                if (fileValid(fo)) {
                    scanFile(fo);
                }
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                Log.warn(String.format("visit file %s failed, exc: %s", file, exc));
                return super.visitFileFailed(file, exc);
            }
        });
    }

    public void scanDir(File dir) throws IOException {
        scanDir(dir.getAbsolutePath());
    }

    public void scanFile(File file) throws IOException {
        Log.info("scan file: " + file.getAbsolutePath());
        String content = new String(Files.readAllBytes(file.toPath()));
        listenableSet.forEach(each -> each.handleContent(content));
    }
}
