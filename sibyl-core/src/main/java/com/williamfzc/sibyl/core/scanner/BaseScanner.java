package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.intf.Listenable;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseScanner {
    protected final List<Listenable> listenableList = new LinkedList<>();

    public void registerListener(Listenable listenable) {
        listenableList.add(listenable);
    }

    // hooks
    protected abstract void beforeEachFile(File file);

    protected abstract void afterEachFile(File file);

    protected abstract void beforeEachListener(Listenable listenable);

    protected abstract void afterEachListener(Listenable listenable);

    protected abstract void afterScan();
}
