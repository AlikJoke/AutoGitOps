package ru.joke.git.commands;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.SubmoduleConfig;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.transport.TagOpt;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("pull")
public final class AutoGitPullCommand implements AutoGitCommand<PullResult, AutoGitPullCommand, AutoGitPullCommand.PullCommandBuilder> {

    private static final ContentMergeStrategy DEFAULT_CONTENT_MERGE_STRATEGY = ContentMergeStrategy.CONFLICT;
    private static final MergeCommand.FastForwardMode DEFAULT_FAST_FORWARD_MODE = MergeCommand.FastForwardMode.FF;
    private static final boolean DEFAULT_REBASE = true;

    private final boolean rebase;
    private final MergeCommand.FastForwardMode fastForwardMode;
    private final BranchConfig.BranchRebaseMode rebaseMode;
    private final String remote;
    private final String branch;
    private final SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode;
    private final ContentMergeStrategy contentMergeStrategy;
    private final TagOpt tagOpt;

    private AutoGitPullCommand() {
        this(
                DEFAULT_REBASE,
                DEFAULT_FAST_FORWARD_MODE,
                null,
                null,
                null,
                null,
                DEFAULT_CONTENT_MERGE_STRATEGY,
                null
        );
    }

    private AutoGitPullCommand(
            final boolean rebase,
            final MergeCommand.FastForwardMode fastForwardMode,
            final BranchConfig.BranchRebaseMode rebaseMode,
            final String remote,
            final String branch,
            final SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode,
            final ContentMergeStrategy contentMergeStrategy,
            final TagOpt tagOpt
    ) {
        this.rebase = rebase;
        this.fastForwardMode = fastForwardMode;
        this.rebaseMode = rebaseMode;
        this.remote = remote;
        this.branch = branch;
        this.recurseSubmodulesMode = recurseSubmodulesMode;
        this.contentMergeStrategy = contentMergeStrategy;
        this.tagOpt = tagOpt;
    }

    @Override
    public PullResult call() {
        try {
            final var pullCommand = GitStorage.getGit().pull();
            if (this.tagOpt != null) {
                pullCommand.setTagOpt(this.tagOpt);
            }

            return pullCommand
                    .setRebase(this.rebase)
                    .setFastForward(this.fastForwardMode)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setRebase(this.rebaseMode)
                    .setRemote(this.remote)
                    .setRemoteBranchName(this.branch)
                    .setRecurseSubmodules(this.recurseSubmodulesMode)
                    .setContentMergeStrategy(this.contentMergeStrategy)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PullCommandBuilder toBuilder() {
        return builder()
                .withBranch(this.branch)
                .withRebase(this.rebase)
                .withRebaseMode(this.rebaseMode)
                .withFastForwardMode(this.fastForwardMode)
                .withRecurseSubmodulesMode(this.recurseSubmodulesMode)
                .withContentMergeStrategy(this.contentMergeStrategy)
                .withRemote(this.remote)
                .withTagOpt(this.tagOpt);
    }

    @Override
    public String toString() {
        return "pull{"
                + "rebase=" + rebase
                + ", fastForwardMode=" + fastForwardMode
                + ", rebaseMode=" + rebaseMode
                + ", remote='" + remote + '\''
                + ", branch='" + branch + '\''
                + ", recurseSubmodulesMode=" + recurseSubmodulesMode
                + ", contentMergeStrategy=" + contentMergeStrategy
                + ", tagOpt=" + tagOpt
                + '}';
    }

    public static PullCommandBuilder builder() {
        return new PullCommandBuilder();
    }

    public static final class PullCommandBuilder implements Builder<PullCommandBuilder, PullResult, AutoGitPullCommand> {

        private boolean rebase = DEFAULT_REBASE;
        private MergeCommand.FastForwardMode fastForwardMode = DEFAULT_FAST_FORWARD_MODE;
        private BranchConfig.BranchRebaseMode rebaseMode;
        private String remote;
        private String branch;
        private SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode;
        private ContentMergeStrategy contentMergeStrategy = DEFAULT_CONTENT_MERGE_STRATEGY;
        private TagOpt tagOpt;

        public PullCommandBuilder withRebase(final boolean rebase) {
            this.rebase = rebase;
            return this;
        }

        public PullCommandBuilder withFastForwardMode(final MergeCommand.FastForwardMode fastForwardMode) {
            this.fastForwardMode = fastForwardMode;
            return this;
        }

        public PullCommandBuilder withTagOpt(final TagOpt tagOpt) {
            this.tagOpt = tagOpt;
            return this;
        }

        public PullCommandBuilder withRebaseMode(final BranchConfig.BranchRebaseMode rebaseMode) {
            this.rebaseMode = rebaseMode;
            return this;
        }

        public PullCommandBuilder withRemote(final String remote) {
            this.remote = remote;
            return this;
        }

        public PullCommandBuilder withBranch(final String branch) {
            this.branch = branch;
            return this;
        }

        public PullCommandBuilder withRecurseSubmodulesMode(final SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode) {
            this.recurseSubmodulesMode = recurseSubmodulesMode;
            return this;
        }

        public PullCommandBuilder withContentMergeStrategy(final ContentMergeStrategy contentMergeStrategy) {
            this.contentMergeStrategy = contentMergeStrategy;
            return this;
        }

        @Override
        public AutoGitPullCommand build() {
            return new AutoGitPullCommand(
                    this.rebase,
                    this.fastForwardMode,
                    this.rebaseMode,
                    this.remote,
                    this.branch,
                    this.recurseSubmodulesMode,
                    this.contentMergeStrategy,
                    this.tagOpt
            );
        }
    }
}
