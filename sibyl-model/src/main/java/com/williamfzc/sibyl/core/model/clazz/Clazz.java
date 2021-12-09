package com.williamfzc.sibyl.core.model.clazz;

import lombok.Data;

import java.util.Set;

@Data
public class Clazz {
    private String name;
    private String packageName;
    private String superName;
    private Set<String> interfaces;
}
