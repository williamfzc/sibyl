package com.williamfzc.sibyl.core.model.diff;

import com.williamfzc.sibyl.core.model.method.Method;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DiffMethod extends Method {
    private List<Integer> diffLines;

    public void safeSetDiffLines(List<Integer> diffLines) {
        this.diffLines =
                diffLines.stream()
                        .filter(each -> getLineRange().contains(each))
                        .collect(Collectors.toList());
    }

    public Integer getDiffCount() {
        return diffLines.size();
    }

    public Float calcDiffScore() {
        return getDiffCount().floatValue() / getLineCount().floatValue();
    }
}
