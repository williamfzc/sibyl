package com.williamfzc.sibyl.core.api;

import com.williamfzc.sibyl.core.api.internal.SibylCallgraph;
import com.williamfzc.sibyl.core.api.internal.SibylDiff;
import com.williamfzc.sibyl.core.api.internal.SibylSnapshot;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.scanner.file.FileIntroScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import java.io.IOException;

public final class Sibyl {
    private static final SibylSnapshot snapshotApi = new SibylSnapshot();
    private static final SibylDiff diffApi = new SibylDiff();
    private static final SibylCallgraph callgraphApi = new SibylCallgraph();

    private Sibyl() {}

    public static void genSnapshotFromDir(File inputDir, File outputFile, SibylLangType lang)
            throws IOException, InterruptedException {
        genSnapshotFromDir(inputDir, outputFile, lang, null);
    }

    public static void genSnapshotFromDir(
            File inputDir, File outputFile, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        snapshotApi.genSnapshotFromDir(inputDir, outputFile, lang, policy);
    }

    public static Storage<Method> genSnapshotFromDir(File inputDir, SibylLangType lang)
            throws IOException, InterruptedException {
        return genSnapshotFromDir(inputDir, lang, null);
    }

    public static Storage<Method> genSnapshotFromDir(
            File inputDir, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        return snapshotApi.genSnapshotFromDir(inputDir, lang, policy);
    }

    public static Storage<DiffMethod> genSnapshotDiff(
            Storage<Method> methodStorage, DiffResult diff, String prefix) {
        return diffApi.genSnapshotDiff(methodStorage, diff, prefix);
    }

    public static Storage<DiffMethod> genSnapshotDiff(
            Storage<Method> methodStorage, DiffResult diff) {
        return genSnapshotDiff(methodStorage, diff, "");
    }

    public static void genCallGraphFromDir(File inputDir, File outputFile, SibylLangType lang)
            throws IOException, InterruptedException {
        callgraphApi.genCallGraphFromDir(inputDir, outputFile, lang);
    }

    public static Storage<File> collectFileFromDir(File inputDir, SibylLangType lang)
            throws IOException, InterruptedException {
        FileIntroScanner scanner = new FileIntroScanner(inputDir);
        IStorableListener<File> listener =
                new IStorableListener<File>() {
                    private Storage<File> data = new Storage<>();

                    @Override
                    public void setStorage(Storage<File> storage) {
                        this.data = storage;
                    }

                    @Override
                    public Storage<File> getStorage() {
                        return data;
                    }

                    @Override
                    public void handle(File file, String content) {
                        this.data.save(file);
                    }

                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith(lang.FILE_SUBFIX);
                    }
                };

        scanner.registerListener(listener);
        scanner.scanDir(inputDir);
        return listener.getStorage();
    }
}
