package com.williamfzc.sibyl.core.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfzc.sibyl.core.analyzer.EdgeAnalyzer;
import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.listener.java8.Java8CallListener;
import com.williamfzc.sibyl.core.listener.java8.Java8ClassListener;
import com.williamfzc.sibyl.core.listener.java8.Java8SnapshotListener;
import com.williamfzc.sibyl.core.listener.kt.KtSnapshotListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.diff.DiffFile;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.NormalScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Sibyl {
    public static void genSnapshotFromDir(File inputDir, File outputFile, SibylLangType lang)
            throws IOException, InterruptedException {
        Storage<Method> methodStorage = genSnapshotFromDir(inputDir, lang);
        if (null != methodStorage) {
            methodStorage.exportFile(outputFile);
        }
    }

    public static Storage<Method> genSnapshotFromDir(File inputDir, SibylLangType lang)
            throws IOException, InterruptedException {
        switch (lang) {
            case JAVA_8:
                return genSnapshotFromDir(inputDir, new Java8SnapshotListener());
            case KOTLIN:
                return genSnapshotFromDir(inputDir, new KtSnapshotListener());
            default:
                break;
        }
        return null;
    }

    // todo: diff type here
    public static Set<Method> genSnapshotDiff(Storage<Method> methodStorage, DiffResult diff) {
        // 1. create a (fileName as key, method as value) map
        // 2. diff foreach: if file changed, check all its methods in map
        // 3. save all the valid methods to a list
        // return

        Map<String, Collection<Method>> methodMap = new HashMap<>();
        Set<Method> diffMethods = new HashSet<>();
        for (Method eachMethod : methodStorage.getData()) {
            String eachFileName = eachMethod.getBelongsTo().getFile().getName();
            methodMap.putIfAbsent(eachFileName, new HashSet<>());
            methodMap.get(eachFileName).add(eachMethod);
        }

        for (DiffFile diffFile : diff.getNewFiles()) {
            String eachFileName = diffFile.getName();
            if (!methodMap.containsKey(eachFileName)) {
                continue;
            }

            // valid file
            methodMap
                    .get(eachFileName)
                    .forEach(
                            eachMethod -> {
                                List<Integer> methodRange = eachMethod.getLineRange();
                                Log.info(
                                        String.format(
                                                "method %s, line range: %s",
                                                eachMethod.getInfo().getName(), methodRange));

                                // hit this method
                                if (diffFile.getLines().stream().anyMatch(methodRange::contains)) {
                                    diffMethods.add(eachMethod);
                                }
                            });
        }

        return diffMethods;
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
        NormalScanner scanner = new NormalScanner(inputDir);

        Storage<Method> methodStorage = new Storage<>();
        listener.setStorage(methodStorage);

        scanner.registerListener(listener);
        scanner.scanDir(inputDir);
        return methodStorage;
    }

    private static Storage<Edge> genJava8CallGraphFromDir(File inputDir)
            throws IOException, InterruptedException {
        NormalScanner scanner = new NormalScanner(inputDir);

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
