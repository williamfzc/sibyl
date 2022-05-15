package com.williamfzc.sibyl.utgen;

import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.test.Support;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TestApi {
    @Test
    public void ok() throws IOException, InterruptedException {
        Storage<Method> ret = (Storage<Method>) UtGen.collectAllMethods(Support.getProjectRoot());
        //        Storage<Method> ret =
        //                (Storage<Method>) UtGen.collectMethods(Support.getProjectRoot(), "HEAD",
        // "HEAD~~~");

        UtGen.methodsToCases(ret)
                .forEach(
                        each -> {
                            try {
                                each.writeTo(
                                        new File(
                                                Support.getProjectRoot(),
                                                "sibyl-ut-generator/src/test/java1"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
    }
}
