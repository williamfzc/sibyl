package com.williamfzc.sibyl.core.model.method;

import com.williamfzc.sibyl.core.model.clazz.Clazz;
import lombok.Data;

@Data
public final class MethodBelonging {
    private Clazz clazz;
    private MethodBelongingFile file;
}
