package com.williamfzc.sibyl.core.storage.snapshot;

import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.model.method.MethodBelonging;
import com.williamfzc.sibyl.core.model.method.MethodBelongingFile;
import com.williamfzc.sibyl.core.storage.Storage;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Snapshot extends Storage<Method> {
    public Set<Method> queryMethodsByFullClazzName(String fullClazzName) {
        return this.getData().stream()
                .filter(
                        each -> {
                            if (null == each.getBelongsTo()) {
                                return false;
                            }
                            if (null == each.getBelongsTo().getClazz()) {
                                return false;
                            }
                            return each.getBelongsTo()
                                    .getClazz()
                                    .getFullName()
                                    .equals(fullClazzName);
                        })
                .collect(Collectors.toSet());
    }

    public Set<String> listClazzName() {
        return this.getData().stream()
                .filter(Objects::nonNull)
                .map(Method::getBelongsTo)
                .filter(Objects::nonNull)
                .map(MethodBelonging::getClazz)
                .filter(Objects::nonNull)
                .map(Clazz::getFullName)
                .collect(Collectors.toSet());
    }

    public Set<String> listFileName() {
        return this.getData().stream()
                .filter(Objects::nonNull)
                .map(Method::getBelongsTo)
                .filter(Objects::nonNull)
                .map(MethodBelonging::getFile)
                .map(MethodBelongingFile::getName)
                .collect(Collectors.toSet());
    }
}
