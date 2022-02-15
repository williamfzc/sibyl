package com.williamfzc.sibyl.core.model.clazz;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class ClazzBelongingFile {
    private String name;
    private Integer startLine;
    private Integer endLine;
}
