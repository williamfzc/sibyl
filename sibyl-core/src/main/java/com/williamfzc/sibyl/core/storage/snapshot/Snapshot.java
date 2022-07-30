package com.williamfzc.sibyl.core.storage.snapshot;

import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.method.*;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public void syncWithIdentity(Identity identity) {
        this.getData()
                .forEach(
                        each -> {
                            MethodInfo info = each.getInfo();
                            // ret type
                            Set<String> retOptions =
                                    identity.queryPathsByName(info.getReturnType());
                            if (!retOptions.isEmpty()) {
                                String afterType = new ArrayList<>(retOptions).get(0);
                                SibylLog.info(
                                        String.format(
                                                "change ret from %s to %s",
                                                info.getReturnType(), afterType));
                                info.setReturnType(afterType);
                            }

                            List<Parameter> parameterList = info.getParams();
                            if (null != parameterList) {
                                for (Parameter parameter : parameterList) {
                                    Set<String> paramOptions =
                                            identity.queryPathsByName(parameter.getType());
                                    if (!paramOptions.isEmpty()) {
                                        String afterType = new ArrayList<>(paramOptions).get(0);
                                        SibylLog.info(
                                                String.format(
                                                        "change ret from %s to %s",
                                                        parameter.getType(), afterType));
                                        parameter.setType(new ArrayList<>(paramOptions).get(0));
                                    }
                                }
                            }
                        });
    }

    public static Snapshot initFrom(File file) throws IOException {
        Snapshot snapshot = new Snapshot();
        snapshot.save(Snapshot.importAsList(file));
        return snapshot;
    }
}
