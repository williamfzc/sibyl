package com.williamfzc.sibyl.core.model.method;

import lombok.Data;

@Data
public final class MethodBelongingFile {
    private String name;
    private Integer startLine = -1;
    private Integer endLine = -1;
}
