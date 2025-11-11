package ru.joke.git.commands;

import java.util.concurrent.Callable;

public interface AutoGitCommand<T> extends Callable<T> {

    @Override
    T call();
}
