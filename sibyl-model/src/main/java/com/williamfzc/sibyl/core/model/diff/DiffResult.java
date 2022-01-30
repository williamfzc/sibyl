package com.williamfzc.sibyl.core.model.diff;

import java.io.File;
import java.util.List;
import lombok.Data;

@Data
public class DiffResult {
    File gitDir;
    String oldCommit;
    String newCommit;
    List<DiffFile> oldFiles;
    List<DiffFile> newFiles;
}
