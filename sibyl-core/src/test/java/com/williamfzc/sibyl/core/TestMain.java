package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.listener.Java8MethodListener;
import com.williamfzc.sibyl.core.scanner.SimpleScanner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestMain {
    @Test
    public void testMain() throws IOException {
        Path currentRelativePath = Paths.get("");
        SimpleScanner scanner = new SimpleScanner();
        scanner.registerListener(new Java8MethodListener());
        scanner.scanDir(new File(currentRelativePath.toAbsolutePath().toString(), "src/main/java"));
    }
}
