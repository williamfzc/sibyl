package com.williamfzc.sibyl.core.model.edge;

import lombok.Data;

@Data
public class RawEdge {
    private String fromMethodName;
    private String callerType;
    private String toMethodName;

    private String type;
    private String statement;
    private Integer line;
}
