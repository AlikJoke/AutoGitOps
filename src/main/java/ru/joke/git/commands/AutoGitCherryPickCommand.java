package ru.joke.git.commands;

import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ClassPathIndexed("cherry-pick")
public final class AutoGitCherryPickCommand implements AutoGitCommand<CherryPickResult, AutoGitCherryPickCommand, AutoGitCherryPickCommand.CherryPickCommandBuilder> {

    private static final boolean DEFAULT_NO_COMMIT = true;
    private static final ContentMergeStrategy DEFAULT_CONTENT_MERGE_STRATEGY = ContentMergeStrategy.CONFLICT;
    private static final MergeStrategy DEFAULT_MERGE_STRATEGY = MergeStrategy.RECURSIVE;

    private final List<String> refs;
    private final boolean noCommit;
    private final ContentMergeStrategy contentMergeStrategy;
    private final MergeStrategy mergeStrategy;

    private AutoGitCherryPickCommand() {
        this(
                null,
                DEFAULT_NO_COMMIT,
                DEFAULT_CONTENT_MERGE_STRATEGY,
                DEFAULT_MERGE_STRATEGY
        );
    }

    private AutoGitCherryPickCommand(
            final List<String> refs,
            final boolean noCommit,
            final ContentMergeStrategy contentMergeStrategy,
            final MergeStrategy mergeStrategy
    ) {
        this.refs = refs;
        this.noCommit = noCommit;
        this.contentMergeStrategy = contentMergeStrategy;
        this.mergeStrategy = mergeStrategy;
    }

    public List<String> getRefs() {
        return Collections.unmodifiableList(this.refs);
    }

    @Override
    public CherryPickResult call() {
        if (this.refs == null || this.refs.isEmpty()) {
            throw new IllegalStateException("Commit hashes is required for cherry-pick command");
        }

        final var git = GitStorage.getGit();
        final var repo = git.getRepository();
        final var cherryPickCommand = git.cherryPick();

        try {
            for (final var ref : this.refs) {
                final var objectId = repo.resolve(ref);
                cherryPickCommand.include(objectId);
            }

            return cherryPickCommand
                    .setNoCommit(this.noCommit)
                    .setStrategy(this.mergeStrategy.getStrategy())
                    .setCherryPickCommitMessageProvider(RevCommit::getFullMessage)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setContentMergeStrategy(this.contentMergeStrategy)
                    .call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CherryPickCommandBuilder toBuilder() {
        return builder()
                .withRefs(this.refs)
                .withNoCommit(this.noCommit)
                .withMergeStrategy(this.mergeStrategy)
                .withContentMergeStrategy(this.contentMergeStrategy);
    }

    @Override
    public String toString() {
        return "cherry-pick{"
                + "refs=" + refs
                + ", noCommit=" + noCommit
                + ", contentMergeStrategy=" + contentMergeStrategy
                + ", mergeStrategy=" + mergeStrategy
                + '}';
    }

    public static CherryPickCommandBuilder builder() {
        return new CherryPickCommandBuilder();
    }

    public static final class CherryPickCommandBuilder implements Builder<AutoGitCherryPickCommand.CherryPickCommandBuilder, CherryPickResult, AutoGitCherryPickCommand> {

        private final List<String> refs = new ArrayList<>();
        private boolean noCommit = DEFAULT_NO_COMMIT;
        private ContentMergeStrategy contentMergeStrategy = DEFAULT_CONTENT_MERGE_STRATEGY;
        private MergeStrategy mergeStrategy = DEFAULT_MERGE_STRATEGY;

        public CherryPickCommandBuilder withRefs(final List<String> refs) {
            this.refs.addAll(refs);
            return this;
        }

        public CherryPickCommandBuilder withCommitHash(final String commitHash) {
            this.refs.add(commitHash);
            return this;
        }

        public CherryPickCommandBuilder withNoCommit(final boolean noCommit) {
            this.noCommit = noCommit;
            return this;
        }

        public CherryPickCommandBuilder withContentMergeStrategy(final ContentMergeStrategy contentMergeStrategy) {
            this.contentMergeStrategy = contentMergeStrategy;
            return this;
        }

        public CherryPickCommandBuilder withMergeStrategy(final MergeStrategy mergeStrategy) {
            this.mergeStrategy = mergeStrategy;
            return this;
        }

        @Override
        public AutoGitCherryPickCommand build() {
            return new AutoGitCherryPickCommand(
                    this.refs,
                    this.noCommit,
                    this.contentMergeStrategy,
                    this.mergeStrategy
            );
        }
    }
}
