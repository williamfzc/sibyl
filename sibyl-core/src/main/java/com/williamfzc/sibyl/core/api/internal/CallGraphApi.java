package com.williamfzc.sibyl.core.api.internal;

import com.williamfzc.sibyl.core.analyzer.EdgeAnalyzer;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.listener.java8.Java8CallListener;
import com.williamfzc.sibyl.core.listener.java8.Java8ClassListener;
import com.williamfzc.sibyl.core.listener.java8.Java8SnapshotListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.file.FileContentScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.storage.callgraph.CallGraph;
import java.io.File;
import java.io.IOException;

public enum CallGraphApi {
    INSTANCE;

    public void genCallGraphFromDir(
            File inputDir,
            File outputFile,
            SibylLangType lang,
            Storage<Method> methodStorage,
            Storage<Clazz> clazzStorage)
            throws IOException, InterruptedException {
        Storage<Edge> edgeStorage =
                genCallGraphFromDir(inputDir, lang, methodStorage, clazzStorage);
        if (null != edgeStorage) {
            edgeStorage.exportFile(outputFile);
        }
    }

    public CallGraph genCallGraphFromDir(
            File inputDir,
            SibylLangType lang,
            Storage<Method> methodStorage,
            Storage<Clazz> clazzStorage)
            throws IOException, InterruptedException {
        switch (lang) {
            case JAVA_8:
                return genJava8CallGraphFromDir(inputDir, methodStorage, clazzStorage);
            case KOTLIN:
                // NOT IMPLEMENTED
                return null;
            default:
                return null;
        }
    }

    private CallGraph genJava8CallGraphFromDir(
            File inputDir, Storage<Method> methodStorage, Storage<Clazz> clazzStorage)
            throws IOException, InterruptedException {
        FileContentScanner scanner = new FileContentScanner(inputDir);

        IStorableListener<Edge> listener = new Java8CallListener();
        CallGraph edgeStorage = new CallGraph();
        listener.setStorage(edgeStorage);
        scanner.registerListener(listener);

        if (null == methodStorage) {
            IStorableListener<Method> snapshotListener = new Java8SnapshotListener();
            methodStorage = new Storage<>();
            snapshotListener.setStorage(methodStorage);
            scanner.registerListener(snapshotListener);
        }

        if (null == clazzStorage) {
            IStorableListener<Clazz> clazzListener = new Java8ClassListener();
            clazzStorage = new Storage<>();
            clazzListener.setStorage(clazzStorage);
            scanner.registerListener(clazzListener);
        }

        scanner.scanDir(inputDir);

        // analyzer
        EdgeAnalyzer analyzer = new EdgeAnalyzer();
        analyzer.setSnapshot(methodStorage);
        analyzer.setClazzGraph(clazzStorage);
        analyzer.analyze(edgeStorage);

        return edgeStorage;
    }
}
