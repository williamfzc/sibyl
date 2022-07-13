package com.williamfzc.sibyl.ext.casegen.model;

import lombok.Data;

@Data
public class UserCase {
    private String methodPath;
    private String request;
    private String response;
}
