package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.PushResult;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("push")
public final class AutoGitPushCommand implements AutoGitCommand<Iterable<PushResult>, AutoGitPushCommand, AutoGitPushCommand.PushCommandBuilder> {

    private final boolean atomic;
    private final boolean dryRun;
    private final boolean force;
    private final String remote;

    private AutoGitPushCommand() {
        this(
                false,
                false,
                false,
                null
        );
    }

    private AutoGitPushCommand(
            final boolean atomic,
            final boolean dryRun,
            final boolean force,
            final String remote
    ) {
        this.atomic = atomic;
        this.dryRun = dryRun;
        this.force = force;
        this.remote = remote;
    }

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

    @Override
    public PushCommandBuilder toBuilder() {
        return builder()
                .withAtomic(this.atomic)
                .withDryRun(this.dryRun)
                .withForce(this.force)
                .withRemote(this.remote);
    }

    @Override
    public String toString() {
        return "push{"
                + "atomic=" + atomic
                + ", dryRun=" + dryRun
                + ", force=" + force
                + ", remote='" + remote + '\''
                + '}';
    }

    public static PushCommandBuilder builder() {
        return new PushCommandBuilder();
    }

    public static final class PushCommandBuilder implements Builder<PushCommandBuilder, Iterable<PushResult>, AutoGitPushCommand> {

        private boolean atomic;
        private boolean dryRun;
        private boolean force;
        private String remote;

        public PushCommandBuilder withAtomic(final boolean atomic) {
            this.atomic = atomic;
            return this;
        }

        public PushCommandBuilder withDryRun(final boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public PushCommandBuilder withForce(final boolean force) {
            this.force = force;
            return this;
        }

        public PushCommandBuilder withRemote(final String remote) {
            this.remote = remote;
            return this;
        }

        @Override
        public AutoGitPushCommand build() {
            return new AutoGitPushCommand(
                    this.atomic,
                    this.dryRun,
                    this.force,
                    this.remote
            );
        }
    }
}
