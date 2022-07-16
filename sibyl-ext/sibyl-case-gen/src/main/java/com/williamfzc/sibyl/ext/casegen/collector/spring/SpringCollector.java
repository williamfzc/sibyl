package com.williamfzc.sibyl.ext.casegen.collector.spring;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.ext.casegen.collector.BaseCollector;

import java.io.File;
import java.io.IOException;

public class SpringCollector extends BaseCollector {
    private static class CollectPolicy extends ScanPolicy {
        @Override
        public boolean shouldExclude(File file) {
            // why not interface:
            // we collect data from runtime via instrumentation,
            // interface will not be actually executed.
            return !file.toPath().getFileName().toString().endsWith("ServiceImpl.java")
                    && !file.toPath().getFileName().toString().endsWith("Controller.java");
        }
    }

    public Snapshot collectServices(File file) throws IOException, InterruptedException {
        return Sibyl.genSnapshotFromDir(file, SibylLangType.JAVA_8, new CollectPolicy());
    }
}
