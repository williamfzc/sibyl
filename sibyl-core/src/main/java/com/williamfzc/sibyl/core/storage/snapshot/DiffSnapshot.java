package com.williamfzc.sibyl.core.storage.snapshot;

import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.storage.Storage;
import java.io.File;
import java.io.IOException;

public class DiffSnapshot extends Storage<DiffMethod> {

    public static DiffSnapshot initFrom(File file) throws IOException {
        DiffSnapshot snapshot = new DiffSnapshot();
        snapshot.save(DiffSnapshot.importAsList(file));
        return snapshot;
    }
}
