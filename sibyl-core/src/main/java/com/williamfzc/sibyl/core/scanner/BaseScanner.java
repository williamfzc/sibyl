package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.intf.Listenable;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseScanner {
    protected final Set<Listenable> listenableSet = new HashSet<>();

    public void registerListener(Listenable listenable) {
        listenableSet.add(listenable);
    }
}
