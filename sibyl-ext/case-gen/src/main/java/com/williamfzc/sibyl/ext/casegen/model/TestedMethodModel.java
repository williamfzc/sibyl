package com.williamfzc.sibyl.ext.casegen.model;

import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import lombok.Data;

import java.util.List;

@Data
public class TestedMethodModel {
    private String clazzFullName;
    private String methodName;
    private List<Parameter> params;
    private String returnType;

    public String getClazzName() {
        return SibylUtils.fullPath2ClazzName(clazzFullName);
    }

    public String getPackageName() {
        return SibylUtils.fullPath2PackageName(clazzFullName);
    }

    public String getClazzLiberalName() {
        String clazzName = getClazzName();
        return Character.toLowerCase(clazzName.charAt(0)) + clazzName.substring(1);
    }

    public String getMethodPath() {
        return clazzFullName + "." + methodName;
    }
}
