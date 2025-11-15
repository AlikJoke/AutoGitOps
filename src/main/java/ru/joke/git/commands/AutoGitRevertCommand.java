package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ClassPathIndexed("revert")
public final class AutoGitRevertCommand implements AutoGitCommand<String, AutoGitRevertCommand, AutoGitRevertCommand.RevertCommandBuilder> {

    private static final MergeStrategy DEFAULT_MERGE_STRATEGY = MergeStrategy.RECURSIVE;

    private final List<String> include;
    private final MergeStrategy mergeStrategy;
    private final boolean insertChangeId;
    private final String ourCommitName;

    private AutoGitRevertCommand() {
        this(
                null,
                DEFAULT_MERGE_STRATEGY,
                false,
                null
        );
    }

    private AutoGitRevertCommand(
            final List<String> include,
            final MergeStrategy mergeStrategy,
            final boolean insertChangeId,
            final String ourCommitName
    ) {
        this.include = include;
        this.mergeStrategy = mergeStrategy;
        this.insertChangeId = insertChangeId;
        this.ourCommitName = ourCommitName;
    }

    @Override
    public String call() {

        if (this.include == null || this.include.isEmpty()) {
            throw new IllegalStateException("Refs is required for revert command");
        }

        final var git = GitStorage.getGit();
        final var repo = git.getRepository();
        final var revertCommand = git.revert();

        try {
            for (final var includeToRevert : this.include) {
                final var ref = repo.resolve(includeToRevert);
                revertCommand.include(ref);
            }

            final var result =
                    revertCommand
                            .setStrategy(this.mergeStrategy.getStrategy())
                            .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                            .setOurCommitName(this.ourCommitName)
                            .setInsertChangeId(this.insertChangeId)
                            .call();

            return result.getId().getName();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RevertCommandBuilder toBuilder() {
        return builder()
                .include(this.include == null ? Collections.emptyList() : this.include)
                .withMergeStrategy(this.mergeStrategy)
                .withOurCommitName(this.ourCommitName)
                .withInsertChangeId(this.insertChangeId);
    }

    @Override
    public String toString() {
        return "revert{"
                + "include=" + include
                + ", mergeStrategy=" + mergeStrategy
                + ", insertChangeId=" + insertChangeId
                + ", ourCommitName='" + ourCommitName + '\''
                + '}';
    }

    public static RevertCommandBuilder builder() {
        return new RevertCommandBuilder();
    }

    public static final class RevertCommandBuilder implements Builder<AutoGitRevertCommand.RevertCommandBuilder, String, AutoGitRevertCommand> {

        private final List<String> include = new ArrayList<>();
        private MergeStrategy mergeStrategy = DEFAULT_MERGE_STRATEGY;
        private boolean insertChangeId;
        private String ourCommitName;

        public RevertCommandBuilder include(final List<String> include) {
            this.include.addAll(include);
            return this;
        }

        public RevertCommandBuilder include(final String include) {
            this.include.add(include);
            return this;
        }

        public RevertCommandBuilder withOurCommitName(final String ourCommitName) {
            this.ourCommitName = ourCommitName;
            return this;
        }

        public RevertCommandBuilder withMergeStrategy(final MergeStrategy mergeStrategy) {
            this.mergeStrategy = mergeStrategy;
            return this;
        }

        public RevertCommandBuilder withInsertChangeId(final boolean insertChangeId) {
            this.insertChangeId = insertChangeId;
            return this;
        }

        @Override
        public AutoGitRevertCommand build() {
            return new AutoGitRevertCommand(
                    this.include,
                    this.mergeStrategy,
                    this.insertChangeId,
                    this.ourCommitName
            );
        }
    }
}
