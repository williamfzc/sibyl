package com.williamfzc.sibyl.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfzc.sibyl.core.analyzer.EdgeAnalyzer;
import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.listener.java8.Java8CallListener;
import com.williamfzc.sibyl.core.listener.java8.Java8SnapshotListener;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.NormalScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestMain {
    @Test
    public void testMain() throws IOException {
        Path currentRelativePath = Paths.get("");
        NormalScanner scanner = new NormalScanner();

        IStorableListener<Method> listener = new Java8SnapshotListener();
        Storage<Method> methodStorage = new Storage<>();
        listener.setStorage(methodStorage);

        scanner.registerListener(listener);
        scanner.scanDir(new File(currentRelativePath.toAbsolutePath().toString(), "src"));

        System.out.println("method count: " + listener.getStorage().size());
        methodStorage.getData().forEach(each -> Log.info(each.toString()));
    }

    @Test
    public void testCallGraph() throws IOException {
        Path currentRelativePath = Paths.get("");
        NormalScanner scanner = new NormalScanner();

        IStorableListener<Edge> listener = new Java8CallListener();
        Storage<Edge> edgeStorage = new Storage<>();
        listener.setStorage(edgeStorage);

        IStorableListener<Method> snapshotListener = new Java8SnapshotListener();
        Storage<Method> methodStorage = new Storage<>();
        snapshotListener.setStorage(methodStorage);

        scanner.registerListener(listener);
        scanner.registerListener(snapshotListener);
        scanner.scanDir(new File(currentRelativePath.toAbsolutePath().toString(), "src"));

        // analyzer
        EdgeAnalyzer analyzer = new EdgeAnalyzer();
        analyzer.setSnapshot(methodStorage);
        analyzer.analyze(edgeStorage);

        System.out.println("edge count: " + listener.getStorage().size());
        List<Edge> notPerfect = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        edgeStorage
                .getData()
                .forEach(
                        each -> {
                            if (!each.perfect()) {
                                notPerfect.add(each);
                            }
                        });
        File f = new File(currentRelativePath.toAbsolutePath().toString(), "notPerfectEdge.json");
        try (FileWriter writer = new FileWriter(f)) {
            writer.write(mapper.writeValueAsString(notPerfect));
        }
    }
}
