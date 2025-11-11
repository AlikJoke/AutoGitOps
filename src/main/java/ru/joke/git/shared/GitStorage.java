package ru.joke.git.shared;

import org.eclipse.jgit.api.Git;

public abstract class GitStorage {

    private static volatile Git defaultGit;

    public static void setGit(Git git) {
        defaultGit = git;
    }

    public static Git getGit() {
        final var result = defaultGit;
        if (result == null) {
            throw new RuntimeException();
        }

        return result;
    }

    private GitStorage() {}
}
