package ru.joke.git.commands;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ClassPathIndexed("merge")
public final class AutoGitMergeCommand implements AutoGitCommand<MergeResult, AutoGitMergeCommand, AutoGitMergeCommand.MergeCommandBuilder> {

    private static final boolean DEFAULT_COMMIT = true;
    private static final ContentMergeStrategy DEFAULT_CONTENT_MERGE_STRATEGY = ContentMergeStrategy.CONFLICT;
    private static final MergeStrategy DEFAULT_MERGE_STRATEGY = MergeStrategy.RECURSIVE;
    private static final MergeCommand.FastForwardMode DEFAULT_FAST_FORWARD_MODE = MergeCommand.FastForwardMode.FF;

    private final List<String> include;
    private final boolean commit;
    private final ContentMergeStrategy contentMergeStrategy;
    private final MergeStrategy mergeStrategy;
    private final boolean insertChangeId;
    private final String message;
    private final MergeCommand.FastForwardMode fastForwardMode;
    private final boolean squash;

    private AutoGitMergeCommand() {
        this(
                null,
                DEFAULT_COMMIT,
                DEFAULT_CONTENT_MERGE_STRATEGY,
                DEFAULT_MERGE_STRATEGY,
                false,
                null,
                DEFAULT_FAST_FORWARD_MODE,
                false
        );
    }

    private AutoGitMergeCommand(
            final List<String> include,
            final boolean commit,
            final ContentMergeStrategy contentMergeStrategy,
            final MergeStrategy mergeStrategy,
            final boolean insertChangeId,
            final String message,
            final MergeCommand.FastForwardMode fastForwardMode,
            final boolean squash
    ) {
        this.include = include;
        this.commit = commit;
        this.contentMergeStrategy = contentMergeStrategy;
        this.mergeStrategy = mergeStrategy;
        this.insertChangeId = insertChangeId;
        this.message = message;
        this.fastForwardMode = fastForwardMode;
        this.squash = squash;
    }

    @Override
    public MergeResult call() {

        if (this.message == null || this.message.isBlank()) {
            throw new IllegalStateException("Message is required for merge command");
        }
        if (this.include == null || this.include.isEmpty()) {
            throw new IllegalStateException("Refs is required for merge command");
        }

        final var git = GitStorage.getGit();
        final var repo = git.getRepository();
        final var mergeCommand = git.merge();

        try {
            for (final var includeToMerge : this.include) {
                final var ref = repo.resolve(includeToMerge);
                mergeCommand.include(ref);
            }

            return mergeCommand
                    .setCommit(this.commit)
                    .setStrategy(this.mergeStrategy.getStrategy())
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setContentMergeStrategy(this.contentMergeStrategy)
                    .setFastForward(this.fastForwardMode)
                    .setMessage(this.message)
                    .setSquash(this.squash)
                    .setInsertChangeId(this.insertChangeId)
                    .call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MergeCommandBuilder toBuilder() {
        return builder()
                .include(this.include)
                .withCommit(this.commit)
                .withMergeStrategy(this.mergeStrategy)
                .withContentMergeStrategy(this.contentMergeStrategy)
                .withMessage(this.message)
                .withSquash(this.squash)
                .withInsertChangeId(this.insertChangeId)
                .withFastForwardMode(this.fastForwardMode);
    }

    @Override
    public String toString() {
        return "merge{"
                + "include=" + include
                + ", commit=" + commit
                + ", contentMergeStrategy=" + contentMergeStrategy
                + ", mergeStrategy=" + mergeStrategy
                + ", insertChangeId=" + insertChangeId
                + ", message='" + message + '\''
                + ", fastForwardMode=" + fastForwardMode
                + ", squash=" + squash
                + '}';
    }

    public static MergeCommandBuilder builder() {
        return new MergeCommandBuilder();
    }

    public static final class MergeCommandBuilder implements Builder<AutoGitMergeCommand.MergeCommandBuilder, MergeResult, AutoGitMergeCommand> {

        private final List<String> include = new ArrayList<>();
        private boolean commit = DEFAULT_COMMIT;
        private ContentMergeStrategy contentMergeStrategy = DEFAULT_CONTENT_MERGE_STRATEGY;
        private MergeStrategy mergeStrategy = MergeStrategy.RECURSIVE;
        private boolean insertChangeId;
        private String message;
        private MergeCommand.FastForwardMode fastForwardMode = MergeCommand.FastForwardMode.FF;
        private boolean squash;

        public MergeCommandBuilder include(final List<String> include) {
            this.include.addAll(include);
            return this;
        }

        public MergeCommandBuilder include(final String include) {
            this.include.add(include);
            return this;
        }

        public MergeCommandBuilder withCommit(final boolean commit) {
            this.commit = commit;
            return this;
        }

        public MergeCommandBuilder withContentMergeStrategy(final ContentMergeStrategy contentMergeStrategy) {
            this.contentMergeStrategy = contentMergeStrategy;
            return this;
        }

        public MergeCommandBuilder withMergeStrategy(final MergeStrategy mergeStrategy) {
            this.mergeStrategy = mergeStrategy;
            return this;
        }

        public MergeCommandBuilder withInsertChangeId(final boolean insertChangeId) {
            this.insertChangeId = insertChangeId;
            return this;
        }

        public MergeCommandBuilder withMessage(final String message) {
            this.message = message;
            return this;
        }

        public MergeCommandBuilder withFastForwardMode(final MergeCommand.FastForwardMode fastForwardMode) {
            this.fastForwardMode = fastForwardMode;
            return this;
        }

        public MergeCommandBuilder withSquash(final boolean squash) {
            this.squash = squash;
            return this;
        }

        @Override
        public AutoGitMergeCommand build() {
            return new AutoGitMergeCommand(
                    this.include,
                    this.commit,
                    this.contentMergeStrategy,
                    this.mergeStrategy,
                    this.insertChangeId,
                    this.message,
                    this.fastForwardMode,
                    this.squash
            );
        }
    }
}
