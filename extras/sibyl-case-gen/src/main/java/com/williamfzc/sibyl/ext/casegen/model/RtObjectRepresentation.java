package com.williamfzc.sibyl.ext.casegen.model;

import com.google.gson.JsonElement;
import lombok.Data;

@Data
public class RtObjectRepresentation {
    // same as ObjRepresentation
    public static final String TYPE_VALUE_PROTOBUF = "protobuf";
    public static final String TYPE_VALUE_JSON = "json";

    private String type;
    private String valueType;
    // could be a null ...
    private JsonElement value;

    public String getValidJsonValue() {
        if (null == value) {
            return "";
        }

        if (value.isJsonNull()) {
            return "";
        }
        return value.getAsString();
    }
}
