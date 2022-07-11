package com.williamfzc.sibyl.ext.casegen.model;

import lombok.Data;

@Data
public class RestfulRequestSchema {
    private HttpMethod method;
    private String url;
}
