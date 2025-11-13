package ru.joke.git.commands;

import ru.joke.classpath.ClassPathIndexed;

@ClassPathIndexed("publish")
public final class AutoGitPublishCommand implements AutoGitCommand<String, AutoGitPublishCommand, AutoGitPublishCommand.PublishCommandBuilder> {

    private final AutoGitPullCommand pull;
    private final AutoGitAddCommand add;
    private final AutoGitCommitCommand commit;
    private final AutoGitPushCommand push;

    private AutoGitPublishCommand() {
        final var pullCommand = AutoGitPullCommand.builder().build();
        final var addCommand = AutoGitAddCommand.builder().build();
        final var pushCommand = AutoGitPushCommand.builder().build();
        this(
                pullCommand,
                addCommand,
                null,
                pushCommand
        );
    }

    private AutoGitPublishCommand(
            final AutoGitPullCommand pull,
            final AutoGitAddCommand add,
            final AutoGitCommitCommand commit,
            final AutoGitPushCommand push
    ) {
        this.pull = pull;
        this.add = add;
        this.commit = commit;
        this.push = push;
    }

    @Override
    public String call() {

        if (this.commit == null) {
            throw new IllegalStateException("Commit config is required for publish command");
        }

        tryPullIfPossible();

        if (!this.add.call()) {
            throw new RuntimeException("Failed to add changes");
        }

        final var commitId = this.commit.call();
        this.push.call();

        return commitId;
    }

    private void tryPullIfPossible() {
        if (this.pull == null) {
            return;
        }

        final var pullResult = this.pull.call();
        if (!pullResult.isSuccessful()) {
            throw new RuntimeException("Unable to pull");
        }
    }

    @Override
    public PublishCommandBuilder toBuilder() {
        return builder()
                .withCommit(this.commit)
                .withAdd(this.add)
                .withPush(this.push)
                .withPull(this.pull);
    }

    @Override
    public String toString() {
        return "publish{"
                + "pull=" + pull
                + ", add=" + add
                + ", commit=" + commit
                + ", push=" + push
                + '}';
    }

    public static PublishCommandBuilder builder() {
        return new PublishCommandBuilder();
    }

    public static final class PublishCommandBuilder implements Builder<AutoGitPublishCommand.PublishCommandBuilder, String, AutoGitPublishCommand> {

        private AutoGitPullCommand pull = AutoGitPullCommand.builder().build();
        private AutoGitAddCommand add = AutoGitAddCommand.builder().build();
        private AutoGitCommitCommand commit;
        private AutoGitPushCommand push = AutoGitPushCommand.builder().build();

        public PublishCommandBuilder withPull(final AutoGitPullCommand pull) {
            this.pull = pull;
            return this;
        }

        public PublishCommandBuilder withAdd(final AutoGitAddCommand add) {
            this.add = add;
            return this;
        }

        public PublishCommandBuilder withCommit(final AutoGitCommitCommand commit) {
            this.commit = commit;
            return this;
        }

        public PublishCommandBuilder withPush(final AutoGitPushCommand push) {
            this.push = push;
            return this;
        }

        @Override
        public AutoGitPublishCommand build() {
            return new AutoGitPublishCommand(
                    this.pull,
                    this.add,
                    this.commit,
                    this.push
            );
        }
    }
}

