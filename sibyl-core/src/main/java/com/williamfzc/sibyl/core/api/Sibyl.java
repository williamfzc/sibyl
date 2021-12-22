package com.williamfzc.sibyl.core.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfzc.sibyl.core.analyzer.EdgeAnalyzer;
import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.listener.java8.Java8CallListener;
import com.williamfzc.sibyl.core.listener.java8.Java8ClassListener;
import com.williamfzc.sibyl.core.listener.java8.Java8SnapshotListener;
import com.williamfzc.sibyl.core.listener.kt.KtSnapshotListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.NormalScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Sibyl {
    public static void genSnapshotFromDir(File inputDir, File outputFile, SibylLangType lang)
            throws IOException, InterruptedException {
        Storage<Method> methodStorage;
        switch (lang) {
            case JAVA_8:
                methodStorage = genSnapshotFromDir(inputDir, new Java8SnapshotListener());
                methodStorage.exportFile(outputFile);
                break;
            case KOTLIN:
                methodStorage = genSnapshotFromDir(inputDir, new KtSnapshotListener());
                methodStorage.exportFile(outputFile);
                break;
            default:
                break;
        }
    }

    public static void genCallGraphFromDir(File inputDir, File outputFile, SibylLangType lang)
            throws IOException, InterruptedException {
        switch (lang) {
            case JAVA_8:
                Storage<Edge> edgeStorage = genJava8CallGraphFromDir(inputDir);
                List<Edge> perfect = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                edgeStorage
                        .getData()
                        .forEach(
                                each -> {
                                    if (each.perfect()) {
                                        perfect.add(each);
                                    }
                                });

                try (FileWriter writer = new FileWriter(outputFile)) {
                    writer.write(mapper.writeValueAsString(perfect));
                }
                break;
            case KOTLIN:
                // NOT IMPLEMENTED
                break;
            default:
                break;
        }
    }

    private static Storage<Method> genSnapshotFromDir(
            File inputDir, IStorableListener<Method> listener)
            throws IOException, InterruptedException {
        NormalScanner scanner = new NormalScanner();

        Storage<Method> methodStorage = new Storage<>();
        listener.setStorage(methodStorage);

        scanner.registerListener(listener);
        scanner.scanDir(inputDir);
        return methodStorage;
    }

    private static Storage<Edge> genJava8CallGraphFromDir(File inputDir)
            throws IOException, InterruptedException {
        NormalScanner scanner = new NormalScanner();

        IStorableListener<Edge> listener = new Java8CallListener();
        Storage<Edge> edgeStorage = new Storage<>();
        listener.setStorage(edgeStorage);

        IStorableListener<Method> snapshotListener = new Java8SnapshotListener();
        Storage<Method> methodStorage = new Storage<>();
        snapshotListener.setStorage(methodStorage);

        IStorableListener<Clazz> clazzListener = new Java8ClassListener();
        Storage<Clazz> clazzStorage = new Storage<>();
        clazzListener.setStorage(clazzStorage);

        scanner.registerListener(listener);
        scanner.registerListener(snapshotListener);
        scanner.registerListener(clazzListener);

        scanner.scanDir(inputDir);

        // analyzer
        EdgeAnalyzer analyzer = new EdgeAnalyzer();
        analyzer.setSnapshot(methodStorage);
        analyzer.setClazzGraph(clazzStorage);
        analyzer.analyze(edgeStorage);

        return edgeStorage;
    }
}
