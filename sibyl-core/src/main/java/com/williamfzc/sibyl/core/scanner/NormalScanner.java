package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.intf.Listenable;
import com.williamfzc.sibyl.core.utils.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class NormalScanner extends BaseScanner {
    private int validFileNum = 0;

    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public boolean fileValid(File file) {
        // by default
        return true;
    }

    public void scanDir(String dirPath) throws IOException, InterruptedException {
        Set<File> todoFiles = new HashSet<>();
        Files.walkFileTree(
                Paths.get(dirPath),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        File fo = file.toFile();
                        if (fileValid(fo)) {
                            todoFiles.add(fo);
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

        // do the real thing
        Log.info("worker size: " + THREAD_POOL_SIZE);
        executor.invokeAll(
                todoFiles.stream()
                        .map(
                                each ->
                                        (Callable<Void>)
                                                () -> {
                                                    scanFile(each);
                                                    return null;
                                                })
                        .collect(Collectors.toList()));

        afterScan();
    }

    public void scanDir(File dir) throws IOException, InterruptedException {
        scanDir(dir.getAbsolutePath());
    }

    public void scanFile(File file) throws IOException {
        Set<Listenable> acceptedListeners =
                listenableList.stream()
                        .filter(each -> each.accept(file))
                        .collect(Collectors.toSet());
        // need no IO
        if (acceptedListeners.isEmpty()) {
            return;
        }

        beforeEachFile(file);
        Log.info("scan file: " + file.getAbsolutePath());
        String content = new String(Files.readAllBytes(file.toPath()));
        acceptedListeners.forEach(
                eachListener -> {
                    beforeEachListener(eachListener);
                    eachListener.handle(file, content);
                    afterEachListener(eachListener);
                });
        afterEachFile(file);
    }

    @Override
    protected void beforeEachFile(File file) {}

    @Override
    protected void afterEachFile(File file) {
        validFileNum++;
    }

    @Override
    protected void beforeEachListener(Listenable listenable) {}

    @Override
    protected void afterEachListener(Listenable listenable) {
        listenable.afterHandle();
    }

    @Override
    protected void afterScan() {
        Log.info("valid file count: " + validFileNum);
        validFileNum = 0;
    }
}
