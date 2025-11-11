package ru.joke.git.commands;

import ru.joke.classpath.ClassPathIndexed;

@ClassPathIndexed("publish")
public final class AutoGitPublishCommand implements AutoGitCommand<String> {

    private final AutoGitPullCommand pull;
    private final AutoGitAddCommand add;
    private final AutoGitCommitCommand commit;
    private final AutoGitPushCommand push;

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

        final var pullResult = this.pull.call();
        if (!pullResult.isSuccessful()) {
            return pullResult.toString();
        }

        if (!this.add.call()) {
            return "Failed to add changes";
        }

        this.commit.call();
        this.push.call();

        return "OK";
    }
}
