package com.williamfzc.sibyl.core.api;

import com.williamfzc.sibyl.core.api.internal.CallGraphApi;
import com.williamfzc.sibyl.core.api.internal.DiffApi;
import com.williamfzc.sibyl.core.api.internal.SnapshotApi;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.scanner.file.FileIntroScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.storage.callgraph.CallGraph;
import com.williamfzc.sibyl.core.storage.snapshot.DiffSnapshot;
import com.williamfzc.sibyl.core.storage.snapshot.Identity;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import java.io.File;
import java.io.IOException;

public final class Sibyl {
    private static final SnapshotApi snapshotApi = SnapshotApi.INSTANCE;
    private static final DiffApi diffApi = DiffApi.INSTANCE;
    private static final CallGraphApi callgraphApi = CallGraphApi.INSTANCE;

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

    public static Snapshot genSnapshotFromDir(File inputDir, SibylLangType lang)
            throws IOException, InterruptedException {
        return genSnapshotFromDir(inputDir, lang, null);
    }

    public static Snapshot genSnapshotFromDir(File inputDir, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        return snapshotApi.genSnapshotFromDir(inputDir, lang, policy);
    }

    public static Identity genIdentityFromDir(File inputDir, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        return snapshotApi.genIdentityFromDir(inputDir, lang, policy);
    }

    public static DiffSnapshot genSnapshotDiff(
            Storage<Method> methodStorage, DiffResult diff, String prefix, Boolean withCallgraph) {
        return diffApi.genSnapshotDiff(methodStorage, diff, prefix, withCallgraph);
    }

    public static DiffSnapshot genSnapshotDiff(
            Storage<Method> methodStorage, DiffResult diff, String prefix) {
        return genSnapshotDiff(methodStorage, diff, prefix, false);
    }

    public static DiffSnapshot genSnapshotDiff(Storage<Method> methodStorage, DiffResult diff) {
        return genSnapshotDiff(methodStorage, diff, "");
    }

    public static void genCallGraphFromDir(
            File inputDir,
            File outputFile,
            SibylLangType lang,
            Storage<Method> methodStorage,
            Storage<Clazz> clazzStorage)
            throws IOException, InterruptedException {
        callgraphApi.genCallGraphFromDir(inputDir, outputFile, lang, methodStorage, clazzStorage);
    }

    public static void genCallGraphFromDir(
            File inputDir, File outputFile, SibylLangType lang, Storage<Method> methodStorage)
            throws IOException, InterruptedException {
        callgraphApi.genCallGraphFromDir(inputDir, outputFile, lang, methodStorage, null);
    }

    public static void genCallGraphFromDir(File inputDir, File outputFile, SibylLangType lang)
            throws IOException, InterruptedException {
        callgraphApi.genCallGraphFromDir(inputDir, outputFile, lang, null, null);
    }

    public static CallGraph genCallGraphFromDir(File inputDir, SibylLangType lang)
            throws IOException, InterruptedException {
        return genCallGraphFromDir(inputDir, lang, null, null);
    }

    public static CallGraph genCallGraphFromDir(
            File inputDir, SibylLangType lang, Storage<Method> methodStorage)
            throws IOException, InterruptedException {
        return genCallGraphFromDir(inputDir, lang, methodStorage, null);
    }

    public static CallGraph genCallGraphFromDir(
            File inputDir,
            SibylLangType lang,
            Storage<Method> methodStorage,
            Storage<Clazz> clazzStorage)
            throws IOException, InterruptedException {
        return callgraphApi.genCallGraphFromDir(inputDir, lang, methodStorage, clazzStorage);
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
