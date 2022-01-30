package com.williamfzc.sibyl.core;

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
}
