package com.williamfzc.sibyl.core.model.method;

import lombok.Data;

@Data
public final class MethodInfo {
    private String name;
    private String signature;
    private String returnType;
}
