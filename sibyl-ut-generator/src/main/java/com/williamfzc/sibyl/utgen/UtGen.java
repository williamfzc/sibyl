package com.williamfzc.sibyl.utgen;

import com.williamfzc.sibyl.core.api.Sibyl;
import com.williamfzc.sibyl.core.api.SibylLangType;
import com.williamfzc.sibyl.core.api.internal.SibylDiff;
import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.scanner.ScanPolicy;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.SibylLog;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

public class UtGen {
    public static void gen(File projectDir) throws IOException, InterruptedException {
        Repository repo = new RepositoryBuilder().findGitDir(projectDir).build();
        ObjectId head = repo.resolve("HEAD");
        ObjectId headParent = repo.resolve("HEAD~~~~");
        SibylLog.info("after: " + head.getName());
        SibylLog.info("before: " + headParent.getName());

        DiffResult diffResult = SibylDiff.diff(repo, head.getName(), headParent.getName());

        ScanPolicy scanPolicy =
                new ScanPolicy() {
                    @Override
                    public boolean shouldExclude(File file) {
                        return diffResult.getNewFiles().stream()
                                .noneMatch(each -> file.getAbsolutePath().endsWith(each.getName()));
                    }
                };

        Storage<Method> methodStorage =
                Sibyl.genSnapshotFromDir(projectDir, SibylLangType.JAVA_8, scanPolicy);
        assert methodStorage != null;
        Storage<DiffMethod> methods = Sibyl.genSnapshotDiff(methodStorage, diffResult);
        SibylLog.info("diff method count: " + methods.size());

        // methods to cases
        methods.getData()
                .forEach(
                        each -> {
                            try {
                                SibylLog.info(
                                        String.format(
                                                "%s.%s",
                                                each.getBelongsTo().getClazz().getFullName(),
                                                each.getInfo().getName()));
                                SibylLog.info(each.getInfo().getParams().toString());
                            } catch (NullPointerException ignored) {

                            }
                        });
    }
}
