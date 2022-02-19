package com.williamfzc.sibyl.core.api.internal;

import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.listener.java8.Java8SnapshotListener;
import com.williamfzc.sibyl.core.listener.kt.KtSnapshotListener;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.scanner.file.FileContentScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import java.io.IOException;

public final class SibylSnapshot {
    public void genSnapshotFromDir(File inputDir, File outputFile, SibylLangType lang)
            throws IOException, InterruptedException {
        genSnapshotFromDir(inputDir, outputFile, lang, null);
    }

    public void genSnapshotFromDir(
            File inputDir, File outputFile, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        Storage<Method> methodStorage = genSnapshotFromDir(inputDir, lang, policy);
        if (null != methodStorage) {
            methodStorage.exportFile(outputFile);
        }
    }

    public Storage<Method> genSnapshotFromDir(File inputDir, SibylLangType lang)
            throws IOException, InterruptedException {
        return genSnapshotFromDir(inputDir, lang, null);
    }

    public Storage<Method> genSnapshotFromDir(File inputDir, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        switch (lang) {
            case JAVA_8:
                return genSnapshotFromDir(inputDir, new Java8SnapshotListener(), policy);
            case KOTLIN:
                return genSnapshotFromDir(inputDir, new KtSnapshotListener(), policy);
            default:
                break;
        }
        return null;
    }

    private static Storage<Method> genSnapshotFromDir(
            File inputDir, IStorableListener<Method> listener, ScanPolicy policy)
            throws IOException, InterruptedException {
        FileContentScanner scanner = new FileContentScanner(inputDir);
        if (null != policy) {
            scanner.setScanPolicy(policy);
        }

        Storage<Method> methodStorage = new Storage<>();
        listener.setStorage(methodStorage);
        scanner.registerListener(listener);
        scanner.scanDir(inputDir);
        return methodStorage;
    }
}
