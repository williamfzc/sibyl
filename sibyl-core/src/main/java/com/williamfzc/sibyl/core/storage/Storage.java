package com.williamfzc.sibyl.core.storage;

import com.williamfzc.sibyl.core.utils.Log;
import java.util.HashSet;
import java.util.Set;

public class Storage<T> {
    final Set<T> data = new HashSet<>();

    public void save(T t) {
        if (null != t) {
            Log.info("collect new info: " + t);
            data.add(t);
        }
    }

    public int size() {
        return data.size();
    }

    public Set<T> getData() {
        return data;
    }
}
