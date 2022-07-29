package com.williamfzc.sibyl.core.storage.snapshot;

import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.storage.Storage;
import java.util.Set;
import java.util.stream.Collectors;

public class Identity extends Storage<Clazz> {
    public Set<Clazz> queryTypesByName(String name) {
        return this.getData().stream()
                .filter(each -> each.getName().equals(name))
                .collect(Collectors.toSet());
    }

    public Set<String> queryPathsByName(String name) {
        return queryTypesByName(name).stream().map(Clazz::getFullName).collect(Collectors.toSet());
    }
}
