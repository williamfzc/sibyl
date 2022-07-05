package com.williamfzc.sibyl.ext.spring.model;

import com.williamfzc.sibyl.core.model.method.Parameter;
import java.util.List;
import lombok.Data;

@Data
public class ServiceCase {
    private String serviceFullName;
    private String methodName;
    private List<Parameter> params;

    public String getServiceClazzName() {
        return serviceFullName.substring(serviceFullName.lastIndexOf('.') + 1);
    }
}
