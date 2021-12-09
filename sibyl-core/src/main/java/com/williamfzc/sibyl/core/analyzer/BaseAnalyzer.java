package com.williamfzc.sibyl.core.analyzer;

import com.williamfzc.sibyl.core.storage.Storage;

public abstract class BaseAnalyzer<T> {
    public abstract Result<T> analyze(Storage<T> storage);
}
