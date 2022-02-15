package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.intf.Listenable;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class BaseFileScanner extends BaseScanner {
    // https://stackoverflow.com/a/38771060/10641498
    // for catching errors happened in threads
    private static class _Executor extends ThreadPoolExecutor {
        public _Executor(
                int corePoolSize,
                int maximumPoolSize,
                long keepAliveTime,
                TimeUnit unit,
                BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        public static _Executor initPool(int poolSize) {
            return new _Executor(
                    poolSize,
                    poolSize,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t == null && r instanceof Future<?>) {
                try {
                    ((Future<?>) r).get();
                } catch (CancellationException ce) {
                    t = ce;
                } catch (ExecutionException ee) {
                    t = ee.getCause();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }
            }
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

    private int currentFileCount = 0;
    protected File baseDir;

    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;
    private final ExecutorService executor = _Executor.initPool(THREAD_POOL_SIZE);

    public BaseFileScanner(File baseDir) {
        this.baseDir = baseDir;
    }

    // todo: scanner policy config?
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
                        SibylLog.warn(String.format("visit file %s failed, exc: %s", file, exc));
                        return super.visitFileFailed(file, exc);
                    }
                });

        // do the real thing
        SibylLog.info("worker size: " + THREAD_POOL_SIZE);
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
        SibylLog.info(
                String.format(
                        "scan file %s, path %s, size: %s",
                        ++currentFileCount, file.getPath(), file.length()));

        String content = getFileContent(file);

        // avoid using abs path
        File finalFile;
        if (null != baseDir) {
            finalFile = getRealFile(file);
        } else {
            finalFile = file;
        }

        acceptedListeners.forEach(
                eachListener -> {
                    beforeEachListener(eachListener);
                    eachListener.handle(finalFile, content);
                    afterEachListener(eachListener);
                });
        afterEachFile(file);
    }

    public abstract String getFileContent(File file) throws IOException;

    @Override
    protected void beforeEachFile(File file) {}

    @Override
    protected void afterEachFile(File file) {}

    @Override
    protected void beforeEachListener(Listenable listenable) {}

    @Override
    protected void afterEachListener(Listenable listenable) {
        listenable.afterHandle();
    }

    @Override
    protected void afterScan() {}

    private File getRealFile(File file) {
        return Paths.get(baseDir.toURI()).relativize(Paths.get(file.toURI())).toFile();
    }
}
