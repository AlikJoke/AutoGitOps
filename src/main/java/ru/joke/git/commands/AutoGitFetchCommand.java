package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.SubmoduleConfig;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TagOpt;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.util.ArrayList;
import java.util.List;

@ClassPathIndexed("fetch")
public final class AutoGitFetchCommand implements AutoGitCommand<FetchResult, AutoGitFetchCommand, AutoGitFetchCommand.FetchCommandBuilder> {

    private static final int DEFAULT_DEPTH = 0;

    private final boolean dryRun;
    private final boolean forceUpdate;
    private final boolean unshallow;
    private final List<String> refSpecs;
    private final int depth;
    private final String remote;
    private final String branch;
    private final SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode;
    private final boolean checkFetchedObjects;
    private final TagOpt tagOpt;
    
    private AutoGitFetchCommand() {
        this(
                false,
                false,
                false,
                0,
                null,
                null,
                null,
                false,
                null,
                null
        );
    }

    private AutoGitFetchCommand(
            final boolean dryRun,
            final boolean forceUpdate,
            final boolean unshallow,
            final int depth,
            final String remote,
            final String branch,
            final SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode,
            final boolean checkFetchedObjects,
            final TagOpt tagOpt,
            final List<String> refSpecs
    ) {
        this.dryRun = dryRun;
        this.forceUpdate = forceUpdate;
        this.unshallow = unshallow;
        this.depth = depth;
        this.remote = remote;
        this.branch = branch;
        this.recurseSubmodulesMode = recurseSubmodulesMode;
        this.checkFetchedObjects = checkFetchedObjects;
        this.tagOpt = tagOpt;
        this.refSpecs = refSpecs;
    }

    @Override
    public FetchResult call() {
        try {
            final var fetchCommand = GitStorage.getGit().fetch();
            if (this.tagOpt != null) {
                fetchCommand.setTagOpt(this.tagOpt);
            }
            
            if (this.refSpecs != null) {
                fetchCommand.setRefSpecs(this.refSpecs.toArray(new String[0]));
            }

            if (this.depth > 0) {
                fetchCommand.setDepth(this.depth);
            }
            
            return fetchCommand
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setRemote(this.remote)
                    .setInitialBranch(this.branch)
                    .setRecurseSubmodules(this.recurseSubmodulesMode)
                    .setCheckFetchedObjects(this.checkFetchedObjects)
                    .setDryRun(this.dryRun)
                    .setForceUpdate(this.forceUpdate)
                    .setUnshallow(this.unshallow)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FetchCommandBuilder toBuilder() {
        return builder()
                .withDryRun(this.dryRun)
                .withDepth(this.depth)
                .withForceUpdate(this.forceUpdate)
                .withBranch(this.branch)
                .withUnshallow(this.unshallow)
                .withCheckFetchedObjects(this.checkFetchedObjects)
                .withRecurseSubmodulesMode(this.recurseSubmodulesMode)
                .withRemote(this.remote)
                .withTagOpt(this.tagOpt);
    }

    @Override
    public String toString() {
        return "fetch{" 
                + "dryRun=" + dryRun 
                + ", forceUpdate=" + forceUpdate 
                + ", unshallow=" + unshallow 
                + ", refSpecs=" + refSpecs 
                + ", depth=" + depth 
                + ", remote='" + remote + '\'' 
                + ", branch='" + branch + '\'' 
                + ", recurseSubmodulesMode=" + recurseSubmodulesMode 
                + ", checkFetchedObjects=" + checkFetchedObjects 
                + ", tagOpt=" + tagOpt 
                + '}';
    }

    public static FetchCommandBuilder builder() {
        return new FetchCommandBuilder();
    }

    public static final class FetchCommandBuilder implements Builder<FetchCommandBuilder, FetchResult, AutoGitFetchCommand> {

        private boolean dryRun;
        private boolean forceUpdate;
        private boolean unshallow;
        private final List<String> refSpecs = new ArrayList<>();
        private int depth = DEFAULT_DEPTH;
        private String remote;
        private String branch;
        private SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode;
        private boolean checkFetchedObjects;
        private TagOpt tagOpt;

        public FetchCommandBuilder withDryRun(final boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public FetchCommandBuilder withForceUpdate(final boolean forceUpdate) {
            this.forceUpdate = forceUpdate;
            return this;
        }

        public FetchCommandBuilder withUnshallow(final boolean unshallow) {
            this.unshallow = unshallow;
            return this;
        }

        public FetchCommandBuilder withDepth(final int depth) {
            this.depth = depth;
            return this;
        }

        public FetchCommandBuilder withRemote(final String remote) {
            this.remote = remote;
            return this;
        }

        public FetchCommandBuilder withBranch(final String branch) {
            this.branch = branch;
            return this;
        }

        public FetchCommandBuilder withRecurseSubmodulesMode(final SubmoduleConfig.FetchRecurseSubmodulesMode recurseSubmodulesMode) {
            this.recurseSubmodulesMode = recurseSubmodulesMode;
            return this;
        }

        public FetchCommandBuilder withCheckFetchedObjects(final boolean checkFetchedObjects) {
            this.checkFetchedObjects = checkFetchedObjects;
            return this;
        }

        public FetchCommandBuilder withTagOpt(final TagOpt tagOpt) {
            this.tagOpt = tagOpt;
            return this;
        }

        @Override
        public AutoGitFetchCommand build() {
            return new AutoGitFetchCommand(
                    this.dryRun,
                    this.forceUpdate,
                    this.unshallow,
                    this.depth,
                    this.remote,
                    this.branch,
                    this.recurseSubmodulesMode,
                    this.checkFetchedObjects,
                    this.tagOpt,
                    this.refSpecs
            );
        }
    }
}
