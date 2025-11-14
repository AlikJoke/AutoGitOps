package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

import java.io.IOException;
import java.util.*;

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
            final var git = GitStorage.getGit();

            final var addCommand = git.add();

            if (this.all) {
                findAllChangesFiles(git.getRepository()).forEach(addCommand::addFilepattern);
            }

            if (this.files != null) {
                this.files.forEach(addCommand::addFilepattern);
            }

            addCommand
                    .setAll(this.all)
                    .setUpdate(this.update)
                    .call();

            return true;
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AddCommandBuilder toBuilder() {
        return builder()
                .withAll(this.all)
                .withUpdate(this.update)
                .withFilesPatterns(this.files == null ? Collections.emptyList() : this.files);
    }

    @Override
    public String toString() {
        return "add{"
                + "all=" + all
                + ", update=" + update
                + ", files=" + files
                + '}';
    }

    private Set<String> findAllChangesFiles(final Repository repo) throws IOException {
        ObjectId head;
        try (final var rw = new RevWalk(repo)) {
            final var headRef = repo.exactRef(Constants.HEAD);
            head = headRef == null ? null : headRef.getObjectId();
        }

        final var fileTreeIt = new FileTreeIterator(repo);
        final var diff = new IndexDiff(repo, head == null ? "" : head.name(), fileTreeIt);
        diff.diff();

        final Set<String> result = new HashSet<>();
        result.addAll(diff.getUntracked());
        result.addAll(diff.getAdded());
        result.addAll(diff.getChanged());
        result.addAll(diff.getModified());
        result.addAll(diff.getRemoved());
        result.addAll(diff.getMissing());

        return result;
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
