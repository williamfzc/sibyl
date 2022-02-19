package com.williamfzc.sibyl.core.model.diff;

import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.model.method.MethodBelonging;
import com.williamfzc.sibyl.core.model.method.MethodBelongingFile;
import com.williamfzc.sibyl.core.model.method.MethodInfo;
import java.util.Collection;
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

    public void safeSetDiffLines(Collection<Integer> diffLines) {
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

    public static DiffMethod createUnknown() {
        DiffMethod dm = new DiffMethod();
        MethodInfo info = new MethodInfo();
        info.setName(UNKNOWN_NAME);
        dm.setInfo(info);

        MethodBelonging belonging = new MethodBelonging();
        belonging.setClazz(new Clazz());
        belonging.setFile(new MethodBelongingFile());
        dm.setBelongsTo(belonging);
        return dm;
    }
}
