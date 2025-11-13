package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ClassPathIndexed("add")
public final class AutoGitAddCommand implements AutoGitCommand<Boolean, AutoGitAddCommand, AutoGitAddCommand.AddCommandBuilder> {

    private static final boolean DEFAULT_ALL = true;

    private final boolean all;
    private final boolean update;
    private final List<String> files;

    private AutoGitAddCommand() {
        this(
                DEFAULT_ALL,
                false,
                Collections.emptyList()
        );
    }

    private AutoGitAddCommand(
            final boolean all,
            final boolean update,
            final List<String> files
    ) {
        this.all = all;
        this.update = update;
        this.files = files;
    }

    @Override
    public Boolean call() {
        if (!this.update && !this.all && (this.files == null || this.files.isEmpty())) {
            throw new IllegalStateException("Files patterns to add is required for add command");
        }

        try {
            final var addCommand = GitStorage.getGit().add();
            if (this.files != null) {
                this.files.forEach(addCommand::addFilepattern);
            }

            addCommand
                    .setAll(this.all)
                    .setUpdate(this.update)
                    .call();

            return true;
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AddCommandBuilder toBuilder() {
        return builder()
                .withAll(this.all)
                .withUpdate(this.update)
                .withFilesPatterns(this.files);
    }

    @Override
    public String toString() {
        return "add{"
                + "all=" + all
                + ", update=" + update
                + ", files=" + files
                + '}';
    }

    public static AddCommandBuilder builder() {
        return new AddCommandBuilder();
    }

    public static final class AddCommandBuilder implements Builder<AddCommandBuilder, Boolean, AutoGitAddCommand> {

        private final List<String> files = new ArrayList<>();
        private boolean all = DEFAULT_ALL;
        private boolean update;

        public AddCommandBuilder withAll(final boolean all) {
            this.all = all;
            return this;
        }

        public AddCommandBuilder withUpdate(final boolean update) {
            this.update = update;
            return this;
        }

        public AddCommandBuilder withFilesPattern(final String filePattern) {
            this.files.add(filePattern);
            return this;
        }

        public AddCommandBuilder withFilesPatterns(final List<String> filesPatterns) {
            this.files.addAll(filesPatterns);
            return this;
        }

        @Override
        public AutoGitAddCommand build() {
            return new AutoGitAddCommand(
                    this.all,
                    this.update,
                    this.files
            );
        }
    }
}
