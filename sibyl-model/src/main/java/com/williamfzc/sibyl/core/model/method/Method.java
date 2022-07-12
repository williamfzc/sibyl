package com.williamfzc.sibyl.core.model.method;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class Method {
    public static final String UNKNOWN_NAME = "__unknown_method__";
    private MethodInfo info;
    private MethodBelonging belongsTo;

    // todo: what about empty lines?
    public List<Integer> getLineRange() {
        MethodBelongingFile f = belongsTo.getFile();
        if (null == f) {
            return new ArrayList<>();
        }

        List<Integer> ret = new ArrayList<>();
        for (int i = f.getStartLine(); i <= f.getEndLine(); i++) {
            ret.add(i);
        }
        return ret;
    }

    public Integer getLineCount() {
        return getLineRange().size();
    }
}
