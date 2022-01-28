package com.williamfzc.sibyl.core.model.method;

import java.util.List;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public final class MethodInfo {
    private String name;
    private List<Parameter> params;
    private String returnType;
}
