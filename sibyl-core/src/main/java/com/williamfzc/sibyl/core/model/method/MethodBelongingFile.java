package com.williamfzc.sibyl.core.model.method;

import lombok.Data;

@Data
public final class MethodBelongingFile {
    private String file;
    private Integer startLine;
    private Integer endLine;
}
