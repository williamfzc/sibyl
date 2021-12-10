package com.williamfzc.sibyl.core.model.edge;

import com.williamfzc.sibyl.core.model.method.Method;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class Edge {
    private Method source;
    private Method target;
    private String type;
    private String statement;
    private String line;

    // for getting target object
    private RawEdge rawEdge;

    public boolean perfect() {
        return (source != null) && (target != null);
    }
}