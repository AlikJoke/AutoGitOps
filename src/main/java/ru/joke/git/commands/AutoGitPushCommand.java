package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("push")
public final class AutoGitPushCommand implements AutoGitCommand<Iterable<PushResult>> {

    private boolean atomic;
    private boolean dryRun;
    private boolean force;
    private String remote;

    @Override
    public Iterable<PushResult> call() {
        final var pushCommand = GitStorage.getGit().push();
        try {
            return pushCommand
                    .setAtomic(this.atomic)
                    .setDryRun(this.dryRun)
                    .setForce(this.force)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setRemote(this.remote)
                    .setHookErrorStream(System.err)
                    .setHookOutputStream(System.out)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean atomic() {
        return atomic;
    }

    public void setAtomic(boolean atomic) {
        this.atomic = atomic;
    }

    public boolean dryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean force() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String remote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }
}
