package com.williamfzc.sibyl.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Support {
    public static File getSelfSource() {
        Path currentRelativePath = Paths.get("");
        return new File(currentRelativePath.toAbsolutePath().toString(), "src");
    }

    public static File getWorkspace() {
        Path currentRelativePath = Paths.get("");
        File ws = new File(currentRelativePath.toAbsolutePath().toString(), "tmp");
        if (!ws.isDirectory()) {
            boolean ok = ws.mkdir();
            assert ok;
        }
        return ws;
    }
}
