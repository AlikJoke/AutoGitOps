package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.util.Map;
import java.util.Set;

@ClassPathIndexed("status")
public final class AutoGitStatusCommand implements AutoGitCommand<AutoGitStatusCommand.Status, AutoGitStatusCommand, AutoGitStatusCommand.StatusCommandBuilder> {

    private final SubmoduleWalk.IgnoreSubmoduleMode ignoreSubmoduleMode;

    private AutoGitStatusCommand() {
        this(null);
    }

    private AutoGitStatusCommand(final SubmoduleWalk.IgnoreSubmoduleMode ignoreSubmoduleMode) {
        this.ignoreSubmoduleMode = ignoreSubmoduleMode;
    }

    @Override
    public Status call() {
        final var statusCommand = GitStorage.getGit().status();
        try {
            final var result =
                    statusCommand
                            .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                            .setIgnoreSubmodules(this.ignoreSubmoduleMode)
                            .call();

            return new Status(
                    result.getAdded(),
                    result.getChanged(),
                    result.getRemoved(),
                    result.getMissing(),
                    result.getModified(),
                    result.getUntracked(),
                    result.getConflictingStageState(),
                    result.getUncommittedChanges()
            );
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StatusCommandBuilder toBuilder() {
        return builder()
                .withIgnoreSubmoduleMode(this.ignoreSubmoduleMode);
    }

    @Override
    public String toString() {
        return "status{" 
                + "ignoreSubmoduleMode=" + ignoreSubmoduleMode 
                + '}';
    }

    public static StatusCommandBuilder builder() {
        return new StatusCommandBuilder();
    }

    public record Status(
            Set<String> added,
            Set<String> changed,
            Set<String> removed,
            Set<String> missing,
            Set<String> modified,
            Set<String> untracked,
            Map<String, IndexDiff.StageState> conflicts,
            Set<String> uncommitedChanges
    ) {}

    public static final class StatusCommandBuilder implements Builder<StatusCommandBuilder, Status, AutoGitStatusCommand> {

        private SubmoduleWalk.IgnoreSubmoduleMode ignoreSubmoduleMode;

        public StatusCommandBuilder withIgnoreSubmoduleMode(final SubmoduleWalk.IgnoreSubmoduleMode ignoreSubmoduleMode) {
            this.ignoreSubmoduleMode = ignoreSubmoduleMode;
            return this;
        }

        @Override
        public AutoGitStatusCommand build() {
            return new AutoGitStatusCommand(this.ignoreSubmoduleMode);
        }
    }
}
