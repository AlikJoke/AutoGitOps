package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("pack-refs")
public final class AutoGitPackRefsCommand implements AutoGitCommand<String, AutoGitPackRefsCommand, AutoGitPackRefsCommand.PackRefsCommandBuilder> {

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
            final var PackRefsCommandBuilder = git.packRefs();

            return PackRefsCommandBuilder
                    .setAll(this.all)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PackRefsCommandBuilder toBuilder() {
        return builder()
                .withAll(this.all);
    }

    @Override
    public String toString() {
        return "pack-refs{"
                + "all=" + all
                + '}';
    }

    public static PackRefsCommandBuilder builder() {
        return new PackRefsCommandBuilder();
    }

    public static final class PackRefsCommandBuilder implements Builder<PackRefsCommandBuilder, String, AutoGitPackRefsCommand> {

        private boolean all = DEFAULT_ALL;

        public PackRefsCommandBuilder withAll(final boolean all) {
            this.all = all;
            return this;
        }

        @Override
        public AutoGitPackRefsCommand build() {
            return new AutoGitPackRefsCommand(this.all);
        }
    }
}
