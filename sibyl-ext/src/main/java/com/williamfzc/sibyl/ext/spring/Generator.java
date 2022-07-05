package com.williamfzc.sibyl.ext.spring;

import com.williamfzc.sibyl.core.model.method.Parameter;
import com.williamfzc.sibyl.core.storage.snapshot.Snapshot;
import com.williamfzc.sibyl.ext.spring.model.RestfulRequestSchema;
import com.williamfzc.sibyl.ext.spring.model.ServiceCase;
import java.util.List;
import java.util.stream.Collectors;

public class Generator {
    public List<RestfulRequestSchema> genRequests(Snapshot controllers, Snapshot entities) {
        return null;
    }

    public List<ServiceCase> genServiceCases(Snapshot services) {
        return services.getData().stream()
                .map(
                        eachMethod -> {
                            String fullName = eachMethod.getBelongsTo().getClazz().getFullName();
                            String methodName = eachMethod.getInfo().getName();
                            List<Parameter> params = eachMethod.getInfo().getParams();
                            ServiceCase curCase = new ServiceCase();
                            curCase.setServiceFullName(fullName);
                            curCase.setMethodName(methodName);
                            curCase.setParams(params);
                            return curCase;
                        })
                .collect(Collectors.toList());
    }
}
