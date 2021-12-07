package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.listener.java8.Java8MethodListener;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.NormalScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class TestMain {
    @Test
    public void testMain() throws IOException {
        Path currentRelativePath = Paths.get("");
        NormalScanner scanner = new NormalScanner();

        IStorableListener<Method> listener = new Java8MethodListener();
        Storage<Method> methodStorage = new Storage<>();
        listener.setStorage(methodStorage);

        scanner.registerListener(listener);
        scanner.scanDir(new File(currentRelativePath.toAbsolutePath().toString(), "src"));

        System.out.println("method count: " + listener.getStorage().size());
        listener.getStorage()
                .getData()
                .forEach(each -> System.out.println("get method id: " + each.getId()));
    }
}
