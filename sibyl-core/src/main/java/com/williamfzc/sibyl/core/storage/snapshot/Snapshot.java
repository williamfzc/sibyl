package com.williamfzc.sibyl.core.storage.snapshot;

import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import java.util.Set;
import java.util.stream.Collectors;

public class Snapshot extends Storage<Method> {
    public Set<Method> queryMethodsByFullClazzName(String fullClazzName) {
        return this.getData().stream()
                .filter(each -> each.getBelongsTo().getClazz().getFullName().equals(fullClazzName))
                .collect(Collectors.toSet());
    }

    public Set<Method> queryMethodsByFileName(String fileName) {
        return this.getData().stream()
                .filter(each -> each.getBelongsTo().getFile().getName().equals(fileName))
                .collect(Collectors.toSet());
    }

    public Set<String> listClazzName() {
        return this.getData().stream()
                .map(each -> each.getBelongsTo().getClazz().getFullName())
                .collect(Collectors.toSet());
    }

    public Set<String> listFileName() {
        return this.getData().stream()
                .map(each -> each.getBelongsTo().getFile().getName())
                .collect(Collectors.toSet());
    }
}
