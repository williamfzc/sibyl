package com.williamfzc.sibyl.ext.spring.exporter;

import com.williamfzc.sibyl.ext.spring.model.UserCase;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseExporter {
    protected Map<String, Set<UserCase>> userCaseData = new HashMap<>();

    public void importUserCase(String line) {
        List<String> params = Arrays.stream(line.split("\\|")).collect(Collectors.toList());
        if (params.size() != 4) {
            // something wrong
            return;
        }
        UserCase recordCase = new UserCase();
        recordCase.setMethodPath(params.get(0) + "." + params.get(1));
        recordCase.setRequest(params.get(2));
        recordCase.setResponse(params.get(3));

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
