package com.williamfzc.sibyl.ext.casegen;

import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.ext.casegen.model.TestedMethodModel;
import java.util.List;
import java.util.stream.Collectors;

public class Processor {
    public List<TestedMethodModel> genTestedMethodModels(Snapshot snapshot) {
        return snapshot.getData().stream()
                .map(
                        eachMethod -> {
                            String fullName = eachMethod.getBelongsTo().getClazz().getFullName();
                            String methodName = eachMethod.getInfo().getName();
                            List<Parameter> params = eachMethod.getInfo().getParams();
                            TestedMethodModel curModel = new TestedMethodModel();
                            curModel.setClazzFullName(fullName);
                            curModel.setMethodName(methodName);
                            curModel.setParams(params);
                            curModel.setReturnType(eachMethod.getInfo().getReturnType());
                            return curModel;
                        })
                .collect(Collectors.toList());
    }
}