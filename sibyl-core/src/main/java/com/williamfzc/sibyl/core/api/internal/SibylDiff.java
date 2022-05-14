package com.williamfzc.sibyl.core.api.internal;

import com.github.difflib.patch.Patch;
import com.github.difflib.unifieddiff.UnifiedDiff;
import com.github.difflib.unifieddiff.UnifiedDiffFile;
import com.github.difflib.unifieddiff.UnifiedDiffReader;
import com.williamfzc.sibyl.core.model.diff.DiffFile;
import com.williamfzc.sibyl.core.model.diff.DiffMethod;
import com.williamfzc.sibyl.core.model.diff.DiffResult;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.model.method.MethodBelonging;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.storage.snapshot.DiffSnapshot;
import com.williamfzc.sibyl.core.utils.SibylLog;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public final class SibylDiff {
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

    // todo: export this api
    private static void handleDiff(byte[] data, DiffResult diffResult) throws IOException {
        List<UnifiedDiffFile> files = Objects.requireNonNull(data2diff(data)).getFiles();

        List<DiffFile> oldFiles =
                files.stream()
                        .map(
                                each ->
                                        handleOldPatch(
                                                // git will always use unix format path so we
                                                // convert
                                                SibylUtils.formatPath(each.getFromFile()),
                                                each.getPatch()))
                        .collect(Collectors.toList());
        List<DiffFile> newFiles =
                files.stream()
                        .map(
                                each ->
                                        handleNewPatch(
                                                SibylUtils.formatPath(each.getToFile()),
                                                each.getPatch()))
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

    // todo: diff type here
    public DiffSnapshot genSnapshotDiff(
            Storage<Method> methodStorage, DiffResult diff, String prefix, Boolean withCallgraph) {
        // 1. create a (fileName as key, method as value) map
        // 2. diff foreach: if file changed, check all its methods in map
        // 3. save all the valid methods to a list
        // return

        // todo: if callgraph, use analyzer to add more methods

        // file path in snapshot SHORTER than file path in git diff
        Map<String, Collection<Method>> methodMap = new HashMap<>();
        for (Method eachMethod : methodStorage.getData()) {
            String eachFileName;
            if ("".equals(prefix)) {
                eachFileName = new File(eachMethod.getBelongsTo().getFile().getName()).getPath();
            } else {
                eachFileName =
                        new File(prefix, eachMethod.getBelongsTo().getFile().getName()).getPath();
            }

            methodMap.putIfAbsent(eachFileName, new HashSet<>());
            methodMap.get(eachFileName).add(eachMethod);
        }
        // easily cause some paths issues ...
        // keep these logs
        SibylLog.debug(methodMap.toString());
        SibylLog.debug(diff.getNewFiles().toString());

        DiffSnapshot diffMethods = new DiffSnapshot();
        for (DiffFile diffFile : diff.getNewFiles()) {
            String eachFileName = diffFile.getName();
            if (!methodMap.containsKey(eachFileName)) {
                continue;
            }

            // diff lines
            Set<Integer> lines = new HashSet<>(diffFile.getLines());
            Set<Integer> hitLines = new HashSet<>();

            SibylLog.debug("cur file: " + eachFileName);
            methodMap
                    .get(eachFileName)
                    .forEach(
                            eachMethod -> {
                                // todo: what about non-hit lines??
                                List<Integer> methodRange = eachMethod.getLineRange();
                                SibylLog.info(
                                        String.format(
                                                "method %s, line range: %s",
                                                eachMethod.getInfo().getName(), methodRange));

                                // hit this method
                                if (lines.stream().anyMatch(methodRange::contains)) {
                                    hitLines.addAll(methodRange);

                                    DiffMethod dm = new DiffMethod();
                                    dm.setInfo(eachMethod.getInfo());
                                    dm.setBelongsTo(eachMethod.getBelongsTo());
                                    dm.safeSetDiffLines(diffFile.getLines());
                                    diffMethods.save(dm);
                                }
                            });

            // some lines did not match any methods?
            lines.removeAll(hitLines);
            // create an unknown method for saving them
            DiffMethod dm = DiffMethod.createUnknown();
            MethodBelonging belonging = dm.getBelongsTo();
            belonging.getFile().setName(diffFile.getName());
            dm.setBelongsTo(belonging);
            dm.safeSetDiffLines(lines);
            diffMethods.save(dm);
        }

        return diffMethods;
    }
}
