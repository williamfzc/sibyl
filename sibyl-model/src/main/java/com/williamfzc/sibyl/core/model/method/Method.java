package com.williamfzc.sibyl.core.model.method;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public final class Method {
    private MethodInfo info;
    private MethodBelonging belongsTo;
}
