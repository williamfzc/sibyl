package com.williamfzc.sibyl.core.model.clazz;

import com.williamfzc.sibyl.core.model.pkg.Pkg;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class ClazzBelonging {
    Pkg pkg;
    ClazzBelongingFile file;
}
