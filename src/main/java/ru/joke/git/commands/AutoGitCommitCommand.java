package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.CommitConfig;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

@ClassPathIndexed("commit")
public final class AutoGitCommitCommand implements AutoGitCommand<String, AutoGitCommitCommand, AutoGitCommitCommand.CommitCommandBuilder> {

    private final boolean all;
    private final boolean amend;
    private final Person author;
    private final Person committer;
    private final CommitConfig.CleanupMode cleanupMode;
    private final boolean defaultClean;
    private final boolean insertChangeId;
    private final String message;
    private final boolean noVerify;
    private final boolean sign;
    private final String signingKey;
    
    private AutoGitCommitCommand() {
        this(
                false,
                false,
                null,
                null,
                CommitConfig.CleanupMode.DEFAULT,
                false,
                false,
                null,
                false,
                false,
                null
        );
    }
    
    private AutoGitCommitCommand(
            final boolean all,
            final boolean amend,
            final Person author,
            final Person committer,
            final CommitConfig.CleanupMode cleanupMode,
            final boolean defaultClean,
            final boolean insertChangeId,
            final String message,
            final boolean noVerify,
            final boolean sign,
            final String signingKey
    ) {
        this.all = all;
        this.amend = amend;
        this.author = author;
        this.committer = committer;
        this.cleanupMode = cleanupMode;
        this.defaultClean = defaultClean;
        this.insertChangeId = insertChangeId;
        this.message = message;
        this.noVerify = noVerify;
        this.sign = sign;
        this.signingKey = signingKey;
    }

    @Override
    public String call() {
        if (this.message == null || this.message.isBlank()) {
            throw new IllegalStateException("Message is required for commit command");
        }

        try {
            final var commitCommand = GitStorage.getGit().commit();
            if (this.committer != null) {
                commitCommand.setCommitter(this.committer.name, this.committer.email);
            }
            if (this.author != null) {
                commitCommand.setAuthor(this.author.name, this.author.email);
            }
            if (this.cleanupMode != null) {
                commitCommand.setCleanupMode(this.cleanupMode);
            }

            return commitCommand
                    .setAll(this.all)
                    .setAmend(this.amend)
                    .setDefaultClean(this.defaultClean)
                    .setHookErrorStream(System.err)
                    .setHookOutputStream(System.out)
                    .setInsertChangeId(this.insertChangeId)
                    .setMessage(this.message)
                    .setNoVerify(this.noVerify)
                    .setSign(this.sign)
                    .setSigningKey(this.signingKey)
                    .call()
                    .getId()
                    .getName();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommitCommandBuilder toBuilder() {
        return builder()
                .withAll(this.all)
                .withAmend(this.amend)
                .withAuthor(this.author)
                .withCommitter(this.committer)
                .withCleanupMode(this.cleanupMode)
                .withDefaultClean(this.defaultClean)
                .withSign(this.sign)
                .withSigningKey(this.signingKey)
                .withMessage(this.message)
                .withNoVerify(this.noVerify)
                .withInsertChangeId(this.insertChangeId);
    }

    @Override
    public String toString() {
        return "commit{"
                + "all=" + all
                + ", amend=" + amend
                + ", author=" + author
                + ", committer=" + committer
                + ", cleanupMode=" + cleanupMode
                + ", defaultClean=" + defaultClean
                + ", insertChangeId=" + insertChangeId
                + ", message='" + message + '\''
                + ", noVerify=" + noVerify
                + ", sign=" + sign
                + ", signingKey='" + signingKey + '\''
                + '}';
    }

    public static CommitCommandBuilder builder() {
        return new CommitCommandBuilder();
    }

    public record Person(String name, String email) {}

    public static final class CommitCommandBuilder implements Builder<AutoGitCommitCommand.CommitCommandBuilder, String, AutoGitCommitCommand> {

        private boolean all;
        private boolean amend;
        private Person author;
        private Person committer;
        private CommitConfig.CleanupMode cleanupMode;
        private boolean defaultClean;
        private boolean insertChangeId;
        private String message;
        private boolean noVerify;
        private boolean sign;
        private String signingKey;

        public CommitCommandBuilder withAll(final boolean all) {
            this.all = all;
            return this;
        }

        public CommitCommandBuilder withAmend(final boolean amend) {
            this.amend = amend;
            return this;
        }

        public CommitCommandBuilder withAuthor(final Person author) {
            this.author = author;
            return this;
        }

        public CommitCommandBuilder withCommitter(final Person committer) {
            this.committer = committer;
            return this;
        }

        public CommitCommandBuilder withCleanupMode(final CommitConfig.CleanupMode cleanupMode) {
            this.cleanupMode = cleanupMode;
            return this;
        }

        public CommitCommandBuilder withDefaultClean(final boolean defaultClean) {
            this.defaultClean = defaultClean;
            return this;
        }

        public CommitCommandBuilder withInsertChangeId(final boolean insertChangeId) {
            this.insertChangeId = insertChangeId;
            return this;
        }

        public CommitCommandBuilder withMessage(final String message) {
            this.message = message;
            return this;
        }

        public CommitCommandBuilder withNoVerify(final boolean noVerify) {
            this.noVerify = noVerify;
            return this;
        }

        public CommitCommandBuilder withSign(final boolean sign) {
            this.sign = sign;
            return this;
        }

        public CommitCommandBuilder withSigningKey(final String signingKey) {
            this.signingKey = signingKey;
            return this;
        }

        @Override
        public AutoGitCommitCommand build() {
            return new AutoGitCommitCommand(
                    this.all,
                    this.amend,
                    this.author,
                    this.committer,
                    this.cleanupMode,
                    this.defaultClean,
                    this.insertChangeId,
                    this.message,
                    this.noVerify,
                    this.sign,
                    this.signingKey
            );
        }
    }
}