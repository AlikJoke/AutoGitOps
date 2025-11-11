package ru.joke.git.commands;

import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.SubmoduleConfig;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("pull")
public final class AutoGitPullCommand implements AutoGitCommand<PullResult> {

    private boolean rebase;
    private MergeCommand.FastForwardMode fastForwardMode = MergeCommand.FastForwardMode.FF;
    private BranchConfig.BranchRebaseMode rebaseMode;
    private String remote;
    private String branch;
    private SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode;
    private ContentMergeStrategy contentMergeStrategy = ContentMergeStrategy.CONFLICT;

    @Override
    public PullResult call() {
        try {
            final var pullCommand = GitStorage.getGit().pull();
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

    public boolean rebase() {
        return rebase;
    }

    public void setRebase(boolean rebase) {
        this.rebase = rebase;
    }

    public MergeCommand.FastForwardMode fastForwardMode() {
        return fastForwardMode;
    }

    public void setFastForwardMode(MergeCommand.FastForwardMode fastForwardMode) {
        this.fastForwardMode = fastForwardMode;
    }

    public BranchConfig.BranchRebaseMode rebaseMode() {
        return rebaseMode;
    }

    public void setRebaseMode(BranchConfig.BranchRebaseMode rebaseMode) {
        this.rebaseMode = rebaseMode;
    }

    public String remote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String branch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode() {
        return recurseSubmodulesMode;
    }

    public void setRecurseSubmodulesMode(SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode) {
        this.recurseSubmodulesMode = recurseSubmodulesMode;
    }

    public ContentMergeStrategy contentMergeStrategy() {
        return contentMergeStrategy;
    }

    public void setContentMergeStrategy(ContentMergeStrategy contentMergeStrategy) {
        this.contentMergeStrategy = contentMergeStrategy;
    }
}
