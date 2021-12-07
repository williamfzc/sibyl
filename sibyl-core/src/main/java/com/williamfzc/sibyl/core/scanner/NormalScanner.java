package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.intf.Listenable;
import com.williamfzc.sibyl.core.utils.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.stream.Collectors;

public class NormalScanner extends BaseScanner {
    public boolean fileValid(File file) {
        // by default
        return true;
    }

    public void scanDir(String dirPath) throws IOException {
        Files.walkFileTree(
                Paths.get(dirPath),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        File fo = file.toFile();
                        if (fileValid(fo)) {
                            scanFile(fo);
                        }
                        return super.visitFile(file, attrs);
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc)
                            throws IOException {
                        Log.warn(String.format("visit file %s failed, exc: %s", file, exc));
                        return super.visitFileFailed(file, exc);
                    }
                });
    }

    public void scanDir(File dir) throws IOException {
        scanDir(dir.getAbsolutePath());
    }

    public void scanFile(File file) throws IOException {
        Set<Listenable> acceptedListeners =
                listenableSet.stream()
                        .filter(each -> each.accept(file))
                        .collect(Collectors.toSet());
        // need no IO
        if (acceptedListeners.isEmpty()) {
            return;
        }

        Log.info("scan file: " + file.getAbsolutePath());
        String content = new String(Files.readAllBytes(file.toPath()));
        acceptedListeners.forEach(each -> each.handleContent(content));
    }
}
