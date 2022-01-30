package com.williamfzc.sibyl.core;

import com.williamfzc.sibyl.core.api.SibylDiff;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import java.io.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class TestGit {
    // this case can not run in github action
    // it requires .git dir
    @Ignore
    @Test
    public void testGitDiff() throws IOException {
        Repository repo = new RepositoryBuilder().findGitDir(Support.getProjectRoot()).build();
        ObjectId head = repo.resolve("HEAD");
        ObjectId headParent = repo.resolve("HEAD^");
        DiffResult diffResult = SibylDiff.diff(repo, head, headParent);
        diffResult
                .getNewFiles()
                .forEach(
                        each -> {
                            System.out.printf(
                                    "file: %s, lines: %s", each.getName(), each.getLines());
                            System.out.println();
                        });
    }
}
