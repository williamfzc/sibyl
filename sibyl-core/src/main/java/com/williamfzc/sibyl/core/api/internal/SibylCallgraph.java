package com.williamfzc.sibyl.core.api.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SibylCallgraph {
    public void genCallGraphFromDir(File inputDir, File outputFile, SibylLangType lang)
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

    private Storage<Edge> genJava8CallGraphFromDir(File inputDir)
            throws IOException, InterruptedException {
        FileContentScanner scanner = new FileContentScanner(inputDir);

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
