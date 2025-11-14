package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ClassPathIndexed("rm")
public final class AutoGitRmCommand implements AutoGitCommand<Boolean, AutoGitRmCommand, AutoGitRmCommand.RmCommandBuilder> {

    private final boolean cached;
    private final Set<String> files;

    private AutoGitRmCommand() {
        this(false, null);
    }

    private AutoGitRmCommand(
            final boolean cached,
            final Set<String> files
    ) {
        this.cached = cached;
        this.files = files == null ? new HashSet<>() : files;
    }

    @Override
    public Boolean call() {
        if (this.files == null || this.files.isEmpty()) {
            throw new IllegalStateException("File patterns is required for rm command");
        }
        
        final var rmCommand = GitStorage.getGit().rm();
        try {
            this.files.forEach(rmCommand::addFilepattern);

            rmCommand
                    .setCached(this.cached)
                    .call();

            return true;
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RmCommandBuilder toBuilder() {
        return builder()
                .withCached(this.cached)
                .withFiles(this.files == null ? Collections.emptySet() : this.files);
    }

    @Override
    public String toString() {
        return "rm{"
                + "cached=" + cached
                + ", files=" + files
                + '}';
    }

    public static RmCommandBuilder builder() {
        return new RmCommandBuilder();
    }

    public static final class RmCommandBuilder implements Builder<RmCommandBuilder, Boolean, AutoGitRmCommand> {

        private final Set<String> files = new HashSet<>();
        private boolean cached;

        public RmCommandBuilder withCached(final boolean cached) {
            this.cached = cached;
            return this;
        }

        public RmCommandBuilder withFiles(final Set<String> filesPatterns) {
            this.files.addAll(filesPatterns);
            return this;
        }

        public RmCommandBuilder withFiles(final String filesPattern) {
            this.files.add(filesPattern);
            return this;
        }

        @Override
        public AutoGitRmCommand build() {
            return new AutoGitRmCommand(
                    this.cached,
                    this.files
            );
        }
    }
}
