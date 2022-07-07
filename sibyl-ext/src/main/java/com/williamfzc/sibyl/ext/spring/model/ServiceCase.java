package com.williamfzc.sibyl.ext.spring.model;

import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.ext.CommonUtils;
import java.util.List;
import lombok.Data;

@Data
public class ServiceCase {
    private String serviceFullName;
    private String methodName;
    private List<Parameter> params;

    public String getServiceClazzName() {
        return CommonUtils.fullPath2ClazzName(serviceFullName);
    }

    public String getServicePackageName() {
        return CommonUtils.fullPath2PackageName(serviceFullName);
    }

    public String getServiceClazzLiberalName() {
        String clazzName = getServiceClazzName();
        return Character.toLowerCase(clazzName.charAt(0)) + clazzName.substring(1);
    }
}
