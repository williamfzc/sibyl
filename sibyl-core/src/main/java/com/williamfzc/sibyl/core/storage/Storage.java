package com.williamfzc.sibyl.core.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Storage<T> {
    private final Set<T> data = Collections.synchronizedSet(new HashSet<>());

    public void save(T t) {
        if (null != t) {
            SibylLog.debug("collect new info: " + t);
            data.add(t);
        }
    }

    public void save(Iterable<T> l) {
        l.forEach(this::save);
    }

    public void save(T[] a) {
        Arrays.stream(a).forEach(this::save);
    }

    public int size() {
        return data.size();
    }

    public Set<T> getData() {
        return data;
    }

    public String export() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);
    }

    public void exportFile(File file) throws IOException {
        String content = export();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    public static <T> List<T> importAsList(String data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(data, new TypeReference<List<T>>() {});
    }

    public static <T> List<T> importAsList(File file) throws IOException {
        return importAsList(SibylUtils.readContent(file));
    }
}
