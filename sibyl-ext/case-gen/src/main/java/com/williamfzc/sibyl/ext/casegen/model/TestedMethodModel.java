package com.williamfzc.sibyl.ext.casegen.model;

import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import lombok.Data;

import java.util.List;

@Data
public class TestedMethodModel {
    private String serviceFullName;
    private String methodName;
    private List<Parameter> params;
    private String returnType;

    public String getServiceClazzName() {
        return SibylUtils.fullPath2ClazzName(serviceFullName);
    }

    public String getServicePackageName() {
        return SibylUtils.fullPath2PackageName(serviceFullName);
    }

    public String getServiceClazzLiberalName() {
        String clazzName = getServiceClazzName();
        return Character.toLowerCase(clazzName.charAt(0)) + clazzName.substring(1);
    }

    public String getMethodPath() {
        return serviceFullName + "." + methodName;
    }
}
