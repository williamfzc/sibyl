package com.williamfzc.sibyl.core.model.method;

import lombok.Data;

@Data
public final class MethodBelonging {
    private String className;
    private String packageName;
    private MethodBelongingFile file;
}
