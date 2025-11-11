package ru.joke.git.commands;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CheckoutResult;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("checkout")
public final class AutoGitCheckoutCommand implements AutoGitCommand<CheckoutResult.Status> {

    private boolean forced;
    private boolean createBranch;
    private boolean orphan;
    private boolean forceRefUpdate;
    private String branch;
    private CheckoutCommand.Stage stage;
    private CreateBranchCommand.SetupUpstreamMode upstreamMode;
    private String startPoint;

    @Override
    public CheckoutResult.Status call() {
        final var checkoutCommand = GitStorage.getGit().checkout();
        try {
            checkoutCommand
                    .setForced(this.forced)
                    .setCreateBranch(this.createBranch)
                    .setOrphan(this.orphan)
                    .setForceRefUpdate(this.forceRefUpdate)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setName(this.branch)
                    .setStage(this.stage)
                    .setUpstreamMode(this.upstreamMode)
                    .setStartPoint(this.startPoint)
                    .call();
            return checkoutCommand.getResult().getStatus();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean forced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public boolean createBranch() {
        return createBranch;
    }

    public void setCreateBranch(boolean createBranch) {
        this.createBranch = createBranch;
    }

    public boolean orphan() {
        return orphan;
    }

    public void setOrphan(boolean orphan) {
        this.orphan = orphan;
    }

    public boolean forceRefUpdate() {
        return forceRefUpdate;
    }

    public void setForceRefUpdate(boolean forceRefUpdate) {
        this.forceRefUpdate = forceRefUpdate;
    }

    public String branch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public CheckoutCommand.Stage stage() {
        return stage;
    }

    public void setStage(CheckoutCommand.Stage stage) {
        this.stage = stage;
    }

    public CreateBranchCommand.SetupUpstreamMode upstreamMode() {
        return upstreamMode;
    }

    public void setUpstreamMode(CreateBranchCommand.SetupUpstreamMode upstreamMode) {
        this.upstreamMode = upstreamMode;
    }

    public String startPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }
}
