package com.williamfzc.sibyl.core.model.clazz;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;

@Jacksonized
@Data
public class Clazz {
    private String name;
    private String packageName;
    private String superName;
    private Set<String> interfaces;
}
