package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.intf.Listenable;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseScanner {
    protected final Set<Listenable> listenableSet = new HashSet<>();

    public void registerListener(Listenable listenable) {
        listenableSet.add(listenable);
    }

    // hooks
    protected abstract void beforeEachFile(File file);

    protected abstract void afterEachFile(File file);

    protected abstract void beforeEachListener(Listenable listenable);

    protected abstract void afterEachListener(Listenable listenable);
}
