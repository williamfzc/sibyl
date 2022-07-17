package com.williamfzc.sibyl.ext.casegen.model.rt;

import com.williamfzc.sibyl.ext.casegen.model.RtObjectRepresentation;
import lombok.Data;

import java.util.List;

@Data
public class UserCase {
    private String methodPath;
    private List<RtObjectRepresentation> request;
    private RtObjectRepresentation response;
}
