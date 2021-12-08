package com.williamfzc.sibyl.core.model.edge;

import lombok.Data;

@Data
public class InvokeInfo {
    private String type;
    private String statement;
    private String line;
}
