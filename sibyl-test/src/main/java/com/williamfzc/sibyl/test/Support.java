package com.williamfzc.sibyl.test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Support {
    public static File getProjectRoot() {
        Path currentRelativePath = Paths.get("");
        return new File(currentRelativePath.toAbsolutePath().toString()).getParentFile();
    }

    public static File getModuleRoot() {
        Path currentRelativePath = Paths.get("");
        return new File(currentRelativePath.toAbsolutePath().toString());
    }

    public static File getCoreRoot() {
        return new File(getProjectRoot(), "sibyl-core");
    }

    public static File getSelfSource() {
        return new File(getModuleRoot(), "src");
    }

    public static File getWorkspace() {
        File ws = new File(getModuleRoot(), "tmp");
        if (!ws.isDirectory()) {
            boolean ok = ws.mkdir();
            assert ok;
        }
        return ws;
    }

    public static File getTestRes() {
        return new File(getProjectRoot(), "testRes");
    }

    public static File getTargetDir() {
        return new File(getModuleRoot(), "target");
    }

    public static File getJar() {
        String ver = System.getProperty("sibylVersion");
        return new File(
                getTargetDir(), String.format("sibyl-cli-%s-jar-with-dependencies.jar", ver));
    }
}
