package ru.joke.git.commands;

import org.eclipse.jgit.merge.StrategyRecursive;
import org.eclipse.jgit.merge.StrategyResolve;

public enum MergeStrategy {
    RESOLVE(new StrategyResolve()),
    RECURSIVE(new StrategyRecursive());

    private final org.eclipse.jgit.merge.MergeStrategy strategy;

    MergeStrategy(final org.eclipse.jgit.merge.MergeStrategy strategy) {
        this.strategy = strategy;
    }

    public org.eclipse.jgit.merge.MergeStrategy getStrategy() {
        return this.strategy;
    }
}
