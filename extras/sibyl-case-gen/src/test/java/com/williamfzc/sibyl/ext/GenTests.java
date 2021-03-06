package com.williamfzc.sibyl.ext;

import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.ext.casegen.collector.spring.SpringCollector;
import com.williamfzc.sibyl.ext.casegen.exporter.junit.SpringJUnitExporter;
import com.williamfzc.sibyl.ext.casegen.model.junit.JUnitCaseFile;
import com.williamfzc.sibyl.ext.casegen.model.rt.TestedMethodModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RunWith(JUnit4.class)
public class GenTests {
    @Test
    public void a() throws IOException, InterruptedException {
        File f = new File("/YOUR_PROJECT_PATH");
        if (!f.exists()) {
            // skip
            return;
        }

        SpringCollector collector = new SpringCollector();
        Snapshot services = collector.collectServices(f);

        List<TestedMethodModel> models = TestedMethodModel.of(services);
        SpringJUnitExporter exporter = new SpringJUnitExporter();
        exporter.importUserCases(new File("../casedata"));
        List<JUnitCaseFile> javaFiles = exporter.models2JavaFiles(models);
        javaFiles.forEach(
                eachJavaFile -> {
                    try {
                        eachJavaFile.writeToDir(new File(f, "test\\java").toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
