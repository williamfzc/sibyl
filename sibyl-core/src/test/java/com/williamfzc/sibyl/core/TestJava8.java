package com.williamfzc.sibyl.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfzc.sibyl.core.analyzer.EdgeAnalyzer;
import com.williamfzc.sibyl.core.listener.base.IStorableListener;
import com.williamfzc.sibyl.core.listener.java8.Java8CallListener;
import com.williamfzc.sibyl.core.listener.java8.Java8ClassListener;
import com.williamfzc.sibyl.core.listener.java8.Java8SnapshotListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.scanner.file.FileContentScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.test.Support;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestJava8 {
    @Test
    public void testMain() throws IOException, InterruptedException {
        File src = Support.getSelfSource();

        FileContentScanner scanner = new FileContentScanner(src);
        ScanPolicy policy =
                new ScanPolicy() {
                    @Override
                    public boolean shouldExclude(File file) {
                        return file.getName().toLowerCase().contains("test");
                    }
                };
        scanner.setScanPolicy(policy);
        Assert.assertEquals(scanner.getScanPolicy(), policy);

        IStorableListener<Method> listener = new Java8SnapshotListener();
        Storage<Method> methodStorage = new Storage<>();
        listener.setStorage(methodStorage);

        scanner.registerListener(listener);
        scanner.scanDir(src);

        System.out.println("method count: " + listener.getStorage().size());
        methodStorage.getData().forEach(each -> SibylLog.info(each.toString()));
    }

    @Test
    public void testClazz() throws IOException, InterruptedException {
        File src = Support.getSelfSource();
        FileContentScanner scanner = new FileContentScanner(src);

        IStorableListener<Clazz> listener = new Java8ClassListener();
        Storage<Clazz> clazzStorage = new Storage<>();
        listener.setStorage(clazzStorage);

        scanner.registerListener(listener);
        scanner.scanDir(src);

        System.out.println("clazz count: " + listener.getStorage().size());
        clazzStorage.getData().forEach(each -> SibylLog.info(each.toString()));
    }

    @Test
    public void testCallGraph() throws IOException, InterruptedException {
        File src = Support.getSelfSource();
        File ws = Support.getWorkspace();
        FileContentScanner scanner = new FileContentScanner(src);

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

        // go
        scanner.scanDir(src);

        // analyzer
        EdgeAnalyzer analyzer = new EdgeAnalyzer();
        analyzer.setSnapshot(methodStorage);
        analyzer.setClazzGraph(clazzStorage);
        analyzer.analyze(edgeStorage);

        System.out.println("edge count: " + listener.getStorage().size());
        List<Edge> notPerfect = new ArrayList<>();
        List<Edge> perfect = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        edgeStorage
                .getData()
                .forEach(
                        each -> {
                            if (each.perfect()) {
                                perfect.add(each);
                            } else {
                                notPerfect.add(each);
                            }
                        });

        File notPerfectEdgeFile = new File(ws, "notPerfectEdge.json");
        File perfectEdgeFile = new File(ws, "perfectEdge.json");
        File snapshotFile = new File(ws, "snapshot.json");
        File clazzGraphFile = new File(ws, "classGraph.json");

        try (FileWriter writer = new FileWriter(notPerfectEdgeFile)) {
            writer.write(mapper.writeValueAsString(notPerfect));
        }
        try (FileWriter writer = new FileWriter(perfectEdgeFile)) {
            writer.write(mapper.writeValueAsString(perfect));
        }
        try (FileWriter writer = new FileWriter(snapshotFile)) {
            writer.write(mapper.writeValueAsString(methodStorage.getData()));
        }
        try (FileWriter writer = new FileWriter(clazzGraphFile)) {
            writer.write(mapper.writeValueAsString(clazzStorage.getData()));
        }
    }
}
