package com.williamfzc.sibyl.core.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.williamfzc.sibyl.core.utils.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Storage<T> {
    final Set<T> data = new HashSet<>();

    public void save(T t) {
        if (null != t) {
            Log.info("collect new info: " + t);
            data.add(t);
        }
    }

    public void save(Iterable<T> l) {
        l.forEach(this::save);
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
}
