package com.williamfzc.sibyl.ext.spring;

import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.ext.spring.exporter.JUnitExporter;
import com.williamfzc.sibyl.ext.spring.model.JUnitCaseFile;
import com.williamfzc.sibyl.ext.spring.model.ServiceCase;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GenTests {
    @Test
    public void a() throws IOException, InterruptedException {
        File f =
                new File(
                        "F:\\workspace\\github\\AgileTC\\case-server\\src");
        Generator generator = new Generator();
        Collector collector = new Collector();
        Snapshot services = collector.collectServices(f);

        List<ServiceCase> cases = generator.genServiceCases(services);
        List<JUnitCaseFile> javaFiles = new JUnitExporter().cases2JavaFiles(cases);
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
