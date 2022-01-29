package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.intf.IStorableListener;
import com.williamfzc.sibyl.core.listener.kt.KtClassListener;
import com.williamfzc.sibyl.core.listener.kt.KtSnapshotListener;
import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.NormalScanner;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.Log;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TestKt {
    @Test
    public void testKt() throws IOException, InterruptedException {
        // todo: add kotlin test res
        File src = Support.getSelfSource();
        NormalScanner scanner = new NormalScanner();

        IStorableListener<Method> listener = new KtSnapshotListener();
        Storage<Method> methodStorage = new Storage<>();
        listener.setStorage(methodStorage);

        IStorableListener<Clazz> clazzListener = new KtClassListener();
        Storage<Clazz> clazzStorage = new Storage<>();
        clazzListener.setStorage(clazzStorage);

        scanner.registerListener(listener);
        scanner.registerListener(clazzListener);
        scanner.scanDir(src);

        System.out.println("method count: " + listener.getStorage().size());
        methodStorage.getData().forEach(each -> Log.info(each.toString()));

        System.out.println("class count: " + clazzListener.getStorage().size());
        clazzStorage.getData().forEach(each -> Log.info(each.toString()));
    }
}
