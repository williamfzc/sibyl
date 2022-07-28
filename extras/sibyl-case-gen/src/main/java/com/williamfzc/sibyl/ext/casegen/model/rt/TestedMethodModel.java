package com.williamfzc.sibyl.ext.casegen.model.rt;

import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static List<TestedMethodModel> of (Snapshot snapshot) {
        return snapshot.getData().stream()
                .map(
                        eachMethod -> {
                            try {
                                String fullName = eachMethod.getBelongsTo().getClazz().getFullName();
                                String methodName = eachMethod.getInfo().getName();
                                List<Parameter> params = eachMethod.getInfo().getParams();
                                TestedMethodModel curModel = new TestedMethodModel();
                                curModel.setClazzFullName(fullName);
                                curModel.setMethodName(methodName);
                                curModel.setParams(params);
                                curModel.setReturnType(eachMethod.getInfo().getReturnType());
                                return curModel;
                            } catch (NullPointerException e) {
                                SibylLog.warn("NPE happened in " + eachMethod.toString());
                                return null;
                            }
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
