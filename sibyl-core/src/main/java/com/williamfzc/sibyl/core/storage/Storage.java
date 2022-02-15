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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    public static <R> Storage<R> import_(String data, Class<R> type)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Storage<R> ret = new Storage<>();
        R[] objects = mapper.readValue(data, new TypeReference<R[]>() {});
        ret.save(objects);
        return ret;
    }

    public static <R> Storage<R> import_(File file, Class<R> type) throws IOException {
        return import_(SibylUtils.readContent(file), type);
    }
}
