package com.williamfzc.sibyl.core.model.method;

import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class Parameter {
    private String type;
    private String name;
}
