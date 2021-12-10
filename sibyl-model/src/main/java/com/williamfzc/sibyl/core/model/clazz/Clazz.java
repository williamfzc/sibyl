package com.williamfzc.sibyl.core.model.clazz;

import java.util.Set;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class Clazz {
    private String name;
    private String packageName;
    private String superName;
    private Set<String> interfaces;
}
