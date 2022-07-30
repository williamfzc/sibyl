package com.williamfzc.sibyl.core.scanner.file;

import com.williamfzc.sibyl.core.listener.base.Listenable;
import com.williamfzc.sibyl.core.scanner.base.BaseScanner;
import com.williamfzc.sibyl.core.utils.SibylLog;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// https://stackoverflow.com/a/38771060/10641498
// for catching errors happened in threads
class ScanExecutor extends ThreadPoolExecutor {
    public ScanExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public static ScanExecutor initPool(int poolSize) {
        return new ScanExecutor(
                poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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

public abstract class BaseFileScanner extends BaseScanner {
    private static final RetryPolicy<Object> POLICY_RETRY =
            RetryPolicy.builder()
                    .handle(OutOfMemoryError.class)
                    .withDelay(Duration.ofSeconds(1))
                    .withMaxRetries(3)
                    .onRetry(
                            e -> {
                                Throwable lastException = e.getLastException();
                                lastException.printStackTrace();
                                SibylLog.warn("oom happened, applying retry policy");
                            })
                    .onFailure(
                            e -> {
                                Throwable lastException = e.getException();
                                lastException.printStackTrace();
                                SibylLog.error("retry policy failed because of oom");
                            })
                    .build();

    private final AtomicInteger currentFileCount = new AtomicInteger(0);
    protected File baseDir;

    private final ThreadPoolExecutor executor = ScanExecutor.initPool(scanPolicy.threadPoolSize);

    public BaseFileScanner(File baseDir) {
        this.baseDir = baseDir;
    }

    public boolean fileValid(File file) {
        return !scanPolicy.shouldExclude(file);
    }

    public void scanDir(String dirPath) throws IOException, InterruptedException {
        // collect files
        List<File> todoFiles = new LinkedList<>();
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
        scanFiles(fileSort(todoFiles));
    }

    private List<File> fileSort(List<File> files) {
        if (files.size() <= 1) {
            return files;
        }

        // re sort for better cpu cost
        List<File> ret = new LinkedList<>();
        List<File> filesBySize =
                files.stream()
                        .sorted(Comparator.comparing(File::length))
                        .collect(Collectors.toList());
        int first = 0;
        int last = filesBySize.size() - 1;
        while (first < last) {
            ret.add(filesBySize.get(first));
            first++;
            if (first == last) {
                break;
            }
            ret.add(filesBySize.get(last));
            last--;
        }
        return ret;
    }

    public void scanDir(File dir) throws IOException, InterruptedException {
        scanDir(dir.getAbsolutePath());
    }

    public void scanFiles(Collection<File> files) throws InterruptedException {
        // do the real thing
        executor.setCorePoolSize(scanPolicy.threadPoolSize);
        SibylLog.info("worker size: " + executor.getCorePoolSize());
        long startTime = System.currentTimeMillis();

        int totalFileCount = files.size();
        SibylLog.info("total files: " + totalFileCount);
        executor.invokeAll(
                files.stream()
                        .map(
                                each ->
                                        (Callable<Void>)
                                                () -> {
                                                    scanFile(each, totalFileCount);
                                                    return null;
                                                })
                        .collect(Collectors.toList()));

        long cost = System.currentTimeMillis() - startTime;
        SibylLog.info("time cost(s): " + cost / 1_000F);
        afterScan();
    }

    public void scanFile(File file, int totalFileCount) throws IOException {
        SibylLog.info(
                String.format(
                        "scan file (%d/%d), path %s, size: %s",
                        currentFileCount.incrementAndGet(),
                        totalFileCount,
                        file.getPath(),
                        file.length()));
        Set<Listenable> acceptedListeners =
                listenableList.stream()
                        .filter(each -> each.accept(file))
                        .collect(Collectors.toSet());
        // need no IO
        if (acceptedListeners.isEmpty()) {
            return;
        }

        beforeEachFile(file);
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
                    Failsafe.with(POLICY_RETRY).run(() -> eachListener.handle(finalFile, content));
                    afterEachListener(eachListener);
                });
        // clean up antlr4
        System.gc();
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
