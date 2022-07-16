package com.williamfzc.sibyl.ext.casegen.exporter;

import lombok.Data;

@Data
public class RtParam {
    // same as ObjRepresentation
    public static final String TYPE_VALUE_PROTOBUF = "protobuf";
    public static final String TYPE_VALUE_JSON = "json";

    private String type;
    private String valueType;
    private String value;
}
