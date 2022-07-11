package com.williamfzc.sibyl.ext.spring.model;

import lombok.Data;

@Data
public class UserCase {
    private String methodPath;
    private String request;
    private String response;
}
