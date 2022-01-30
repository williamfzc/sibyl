package com.williamfzc.sibyl.core.api;

import com.github.difflib.patch.Patch;
import com.github.difflib.unifieddiff.UnifiedDiff;
import com.github.difflib.unifieddiff.UnifiedDiffFile;
import com.github.difflib.unifieddiff.UnifiedDiffReader;
import com.williamfzc.sibyl.core.model.diff.DiffFile;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class SibylDiff {
    public static DiffResult diff(Repository repo, ObjectId oldCommit, ObjectId newCommit)
            throws IOException {
        byte[] data;

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            DiffFormatter df = new DiffFormatter(os);
            df.setRepository(repo);

            df.format(oldCommit, newCommit);
            data = os.toByteArray();
        }

        DiffResult diffResult = new DiffResult();
        diffResult.setGitDir(repo.getDirectory());
        diffResult.setOldCommit(oldCommit.name());
        diffResult.setNewCommit(newCommit.name());
        handleDiff(data, diffResult);
        return diffResult;
    }

    private static void handleDiff(byte[] data, DiffResult diffResult) throws IOException {
        List<UnifiedDiffFile> files = Objects.requireNonNull(data2diff(data)).getFiles();

        List<DiffFile> oldFiles =
                files.stream()
                        .map(each -> handleOldPatch(each.getFromFile(), each.getPatch()))
                        .collect(Collectors.toList());
        List<DiffFile> newFiles =
                files.stream()
                        .map(each -> handleNewPatch(each.getToFile(), each.getPatch()))
                        .collect(Collectors.toList());
        diffResult.setOldFiles(oldFiles);
        diffResult.setNewFiles(newFiles);
    }

    private static UnifiedDiff data2diff(byte[] data) throws IOException {
        try (InputStream is = new ByteArrayInputStream(data)) {
            return UnifiedDiffReader.parseUnifiedDiff(is);
        }
    }

    public static DiffResult diff(Repository repo, String oldCommitId, String newCommitId)
            throws IOException {
        RevCommit oldCommit;
        RevCommit newCommit;

        // commit id can be invalid
        // if not found, directly throw
        try (RevWalk revWalk = new RevWalk(repo)) {
            oldCommit = revWalk.parseCommit(ObjectId.fromString(oldCommitId));
            newCommit = revWalk.parseCommit(ObjectId.fromString(newCommitId));
        }

        return diff(repo, oldCommit, newCommit);
    }

    public static DiffResult diff(File gitDir, String oldCommitId, String newCommitId)
            throws IOException {
        Repository repo = new RepositoryBuilder().findGitDir(gitDir).build();
        return diff(repo, oldCommitId, newCommitId);
    }

    private static DiffFile handleNewPatch(String fileName, Patch<String> patch) {
        DiffFile diffFile = new DiffFile();
        diffFile.setName(fileName);

        List<Integer> changedLines =
                patch.getDeltas().stream()
                        .flatMap(each -> each.getTarget().getChangePosition().stream())
                        .collect(Collectors.toList());
        diffFile.setLines(changedLines);
        return diffFile;
    }

    private static DiffFile handleOldPatch(String fileName, Patch<String> patch) {
        DiffFile diffFile = new DiffFile();
        diffFile.setName(fileName);

        List<Integer> changedLines =
                patch.getDeltas().stream()
                        .flatMap(each -> each.getSource().getChangePosition().stream())
                        .collect(Collectors.toList());
        diffFile.setLines(changedLines);
        return diffFile;
    }
}
