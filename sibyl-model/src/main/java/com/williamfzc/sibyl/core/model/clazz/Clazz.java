package com.williamfzc.sibyl.core.model.clazz;

import java.util.Set;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class Clazz {
    public static final String UNKNOWN_NAME = "__unknown_clazz__";

    private String name;
    private String superName;
    private Set<String> interfaces;

    private ClazzBelonging belongsTo;

    public String getFullName() {
        return String.format("%s.%s", belongsTo.pkg.getName(), name);
    }
}
