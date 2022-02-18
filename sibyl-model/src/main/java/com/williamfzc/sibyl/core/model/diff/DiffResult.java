package com.williamfzc.sibyl.core.model.diff;

import java.io.File;
import java.util.List;
import lombok.Data;

@Data
public class DiffResult {
    File gitDir;
    String oldCommit;
    String newCommit;

    // by default, these paths are always rel path (related to git
    List<DiffFile> oldFiles;
    List<DiffFile> newFiles;
}
