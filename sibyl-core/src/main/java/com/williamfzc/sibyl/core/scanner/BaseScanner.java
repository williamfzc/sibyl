package com.williamfzc.sibyl.core.scanner;

import com.williamfzc.sibyl.core.model.Listenable;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseScanner {
    protected final Set<Listenable> listenableSet = new HashSet<>();

    public boolean registerListener(Listenable listenable) {
        if (!listenableSet.contains(listenable)) {
            listenableSet.add(listenable);
            return true;
        }
        return false;
    }
}
