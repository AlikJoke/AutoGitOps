package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("pack-refs")
public final class AutoGitPackRefsCommand implements AutoGitCommand<String, AutoGitPackRefsCommand, AutoGitPackRefsCommand.PackRefsCommand> {

    private static final boolean DEFAULT_ALL = true;

    private final boolean all;

    private AutoGitPackRefsCommand() {
        this(DEFAULT_ALL);
    }

    private AutoGitPackRefsCommand(final boolean all) {
        this.all = all;
    }

    @Override
    public String call() {
        try {
            final var git = GitStorage.getGit();
            final var packRefsCommand = git.packRefs();

            return packRefsCommand
                    .setAll(this.all)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PackRefsCommand toBuilder() {
        return builder()
                .withAll(this.all);
    }

    @Override
    public String toString() {
        return "pack-refs{"
                + "all=" + all
                + '}';
    }

    public static PackRefsCommand builder() {
        return new PackRefsCommand();
    }

    public static final class PackRefsCommand implements Builder<PackRefsCommand, String, AutoGitPackRefsCommand> {

        private boolean all = DEFAULT_ALL;

        public PackRefsCommand withAll(final boolean all) {
            this.all = all;
            return this;
        }

        @Override
        public AutoGitPackRefsCommand build() {
            return new AutoGitPackRefsCommand(this.all);
        }
    }
}
