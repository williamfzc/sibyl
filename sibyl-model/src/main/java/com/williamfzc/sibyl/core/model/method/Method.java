package com.williamfzc.sibyl.core.model.method;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
public class Method {
    private MethodInfo info;
    private MethodBelonging belongsTo;

    // todo: what about empty lines?
    public List<Integer> getLineRange() {
        List<Integer> ret = new ArrayList<>();
        for (int i = belongsTo.getFile().getStartLine();
                i <= belongsTo.getFile().getEndLine();
                i++) {
            ret.add(i);
        }
        return ret;
    }

    public Integer getLineCount() {
        return getLineRange().size();
    }
}
