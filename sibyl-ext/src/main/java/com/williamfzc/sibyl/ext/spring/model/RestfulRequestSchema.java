package com.williamfzc.sibyl.ext.spring.model;

import lombok.Data;

@Data
public class RestfulRequestSchema {
    private HttpMethod method;
    private String url;
}
