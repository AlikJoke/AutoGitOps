package ru.joke.git.shared;

import org.eclipse.jgit.lib.ProgressMonitor;

public abstract class ProgressMonitorStorage {

    private static volatile ProgressMonitor defaultProgressMonitor;

    public static void setProgressMonitor(ProgressMonitor progressMonitor) {
        defaultProgressMonitor = progressMonitor;
    }

    public static ProgressMonitor getProgressMonitor() {
        final var result = defaultProgressMonitor;
        if (result == null) {
            throw new RuntimeException();
        }

        return result;
    }

    private ProgressMonitorStorage() {}
}
