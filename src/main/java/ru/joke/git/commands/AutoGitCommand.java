package ru.joke.git.commands;

import java.util.concurrent.Callable;

public interface AutoGitCommand<R, C extends AutoGitCommand<R, C, B>, B extends AutoGitCommand.Builder<B, R, C>> extends Callable<R> {

    @Override
    R call();

    B toBuilder();

    interface Builder<B extends Builder<B, R, C>, R, C extends AutoGitCommand<R, C, B>> {

        C build();
    }
}
