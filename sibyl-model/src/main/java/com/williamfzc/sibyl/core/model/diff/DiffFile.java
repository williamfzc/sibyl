package com.williamfzc.sibyl.core.model.diff;

import java.util.List;
import lombok.Data;

@Data
public class DiffFile {
    String name;

    // all the lines are DeltaType.CHANGE
    List<Integer> lines;
}
