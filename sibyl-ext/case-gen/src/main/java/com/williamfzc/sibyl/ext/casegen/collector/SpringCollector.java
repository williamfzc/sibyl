package com.williamfzc.sibyl.ext.casegen.collector;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;

import java.io.File;
import java.io.IOException;

public class SpringCollector extends BaseCollector {
    private static class ControllerPolicy extends ScanPolicy {
        @Override
        public boolean shouldExclude(File file) {
            return !file.toPath().getFileName().toString().contains("Controller.java");
        }
    }

    private static class EntityPolicy extends ScanPolicy {
        @Override
        public boolean shouldExclude(File file) {
            return !file.toPath().getFileName().toString().contains("DTO");
        }
    }

    private static class ServicePolicy extends ScanPolicy {
        @Override
        public boolean shouldExclude(File file) {
            // why not interface:
            // we collect data from runtime via instrumentation,
            // interface will not be actually executed.
            return !file.toPath().getFileName().toString().endsWith("ServiceImpl.java");
        }
    }

    public Snapshot collectControllers(File file) throws IOException, InterruptedException {
        return Sibyl.genSnapshotFromDir(file, SibylLangType.JAVA_8, new ControllerPolicy());
    }

    public Snapshot collectEntities(File file) throws IOException, InterruptedException {
        return Sibyl.genSnapshotFromDir(file, SibylLangType.JAVA_8, new EntityPolicy());
    }

    public Snapshot collectServices(File file) throws IOException, InterruptedException {
        return Sibyl.genSnapshotFromDir(file, SibylLangType.JAVA_8, new ServicePolicy());
    }
}
