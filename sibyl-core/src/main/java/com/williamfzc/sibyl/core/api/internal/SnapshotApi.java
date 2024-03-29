package com.williamfzc.sibyl.core.api.internal;

import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.listener.golang.GoSnapshotListener;
import com.williamfzc.sibyl.core.listener.java8.Java8SnapshotListener;
import com.williamfzc.sibyl.core.listener.java8.Java8TypeListener;
import com.williamfzc.sibyl.core.listener.kt.KtSnapshotListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.scanner.file.FileContentScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.storage.snapshot.Identity;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import java.io.File;
import java.io.IOException;

public enum SnapshotApi {
    INSTANCE;

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

    public Snapshot genSnapshotFromDir(File inputDir, SibylLangType lang)
            throws IOException, InterruptedException {
        return genSnapshotFromDir(inputDir, lang, null);
    }

    public Snapshot genSnapshotFromDir(File inputDir, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        switch (lang) {
            case JAVA_8:
                return genSnapshotFromDir(inputDir, new Java8SnapshotListener(), policy);
            case KOTLIN:
                return genSnapshotFromDir(inputDir, new KtSnapshotListener(), policy);
            case GO:
                return genSnapshotFromDir(inputDir, new GoSnapshotListener(), policy);
            default:
                break;
        }
        return null;
    }

    public Identity genIdentityFromDir(File inputDir, SibylLangType lang, ScanPolicy policy)
            throws IOException, InterruptedException {
        switch (lang) {
            case JAVA_8:
                return genIdentityFromDir(inputDir, new Java8TypeListener(), policy);
            default:
                break;
        }
        return null;
    }

    private Identity genIdentityFromDir(
            File inputDir, IStorableListener<Clazz> listener, ScanPolicy policy)
            throws IOException, InterruptedException {
        FileContentScanner scanner = new FileContentScanner(inputDir);
        if (null != policy) {
            scanner.setScanPolicy(policy);
        }

        Identity identity = new Identity();
        listener.setStorage(identity);
        scanner.registerListener(listener);
        scanner.scanDir(inputDir);
        return identity;
    }

    private Snapshot genSnapshotFromDir(
            File inputDir, IStorableListener<Method> listener, ScanPolicy policy)
            throws IOException, InterruptedException {
        FileContentScanner scanner = new FileContentScanner(inputDir);
        if (null != policy) {
            scanner.setScanPolicy(policy);
        }

        Snapshot methodStorage = new Snapshot();
        listener.setStorage(methodStorage);
        scanner.registerListener(listener);
        scanner.scanDir(inputDir);
        return methodStorage;
    }
}
