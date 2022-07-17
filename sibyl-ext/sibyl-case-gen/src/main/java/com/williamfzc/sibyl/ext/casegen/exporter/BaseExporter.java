package com.williamfzc.sibyl.ext.casegen.exporter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.williamfzc.sibyl.ext.casegen.model.RtObjectRepresentation;
import com.williamfzc.sibyl.ext.casegen.model.rt.UserCase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseExporter {
    protected Map<String, Set<UserCase>> userCaseData = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Type requestTypeToken = TypeToken.getParameterized(List.class, RtObjectRepresentation.class).getType();

    private static final String FLAG_FIELD_SPLIT = "\\|,,\\|";
    private static final int PARAM_COUNT = 4;

    public void importUserCase(String line) {
        //
        List<String> params =
                Arrays.stream(line.split(FLAG_FIELD_SPLIT)).collect(Collectors.toList());
        if (params.size() != PARAM_COUNT) {
            // something wrong
            return;
        }
        UserCase recordCase = new UserCase();
        recordCase.setMethodPath(params.get(0) + "." + params.get(1));
        recordCase.setRequest(gson.fromJson(params.get(2), requestTypeToken));
        recordCase.setResponse(gson.fromJson(params.get(3), RtObjectRepresentation.class));

        String key = recordCase.getMethodPath();
        if (!userCaseData.containsKey(key)) {
            userCaseData.put(key, new HashSet<>());
        }
        userCaseData.get(key).add(recordCase);
    }

    public void importUserCases(Iterable<String> lines) {
        lines.forEach(this::importUserCase);
    }

    public void importUserCases(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                importUserCase(line);
            }
        }
    }

    public Map<String, Set<UserCase>> getUserCaseData() {
        return userCaseData;
    }
}
