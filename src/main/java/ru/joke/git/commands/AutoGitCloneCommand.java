package ru.joke.git.commands;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.TagOpt;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.io.File;
import java.util.Collections;
import java.util.Set;

@ClassPathIndexed("clone")
public final class AutoGitCloneCommand implements AutoGitCommand<String, AutoGitCloneCommand, AutoGitCloneCommand.CloneCommandBuilder> {

    private static final boolean DEFAULT_INSTALL_AS_CTX = true;
    private static final boolean DEFAULT_NO_CHECKOUT = false;

    private final boolean installAsContext;
    private final boolean bare;
    private final String initialBranch;
    private final String remote;
    private final String gitDirPath;
    private final boolean cloneAllBranches;
    private final Set<String> branchesToClone;
    private final boolean cloneSubmodules;
    private final int depth;
    private final boolean mirror;
    private final String targetDir;
    private final boolean noCheckout;
    private final TagOpt tags;
    private final String repoUri;

    private AutoGitCloneCommand() {
        this(
                DEFAULT_INSTALL_AS_CTX,
                false,
                null,
                null,
                null,
                false,
                Collections.emptySet(),
                false,
                0,
                false,
                null,
                DEFAULT_NO_CHECKOUT,
                null,
                null
        );
    }

    public AutoGitCloneCommand(
            final boolean installAsContext,
            final boolean bare,
            final String initialBranch,
            final String remote,
            final String gitDirPath,
            final boolean cloneAllBranches,
            final Set<String> branchesToClone,
            final boolean cloneSubmodules,
            final int depth,
            final boolean mirror,
            final String targetDir,
            final boolean noCheckout,
            final TagOpt tags,
            final String repoUri
    ) {
        this.installAsContext = installAsContext;
        this.bare = bare;
        this.initialBranch = initialBranch;
        this.remote = remote;
        this.gitDirPath = gitDirPath;
        this.cloneAllBranches = cloneAllBranches;
        this.branchesToClone = branchesToClone;
        this.cloneSubmodules = cloneSubmodules;
        this.depth = depth;
        this.mirror = mirror;
        this.targetDir = targetDir;
        this.noCheckout = noCheckout;
        this.tags = tags;
        this.repoUri = repoUri;
    }

    @Override
    public String call() {
        try {
            final var cloneCommand = Git.cloneRepository();

            if (this.depth > 0) {
                cloneCommand.setDepth(this.depth);
            }

            final var git =
                    cloneCommand
                            .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                            .setBranch(this.initialBranch)
                            .setBare(this.bare)
                            .setRemote(this.remote)
                            .setGitDir(this.gitDirPath == null ? null : new File(this.gitDirPath))
                            .setBranchesToClone(this.branchesToClone)
                            .setCloneAllBranches(this.cloneAllBranches)
                            .setCloneSubmodules(this.cloneSubmodules)
                            .setDirectory(this.targetDir == null ? null : new File(this.targetDir))
                            .setMirror(this.mirror)
                            .setNoCheckout(this.noCheckout)
                            .setTagOption(this.tags)
                            .setURI(this.repoUri)
                            .call();
            if (this.installAsContext) {
                GitStorage.setGit(git);
            } else {
                git.close();
            }

            return git.getRepository().toString();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CloneCommandBuilder toBuilder() {
        return builder()
                .withBare(this.bare)
                .withBranchesToClone(this.branchesToClone)
                .withDepth(this.depth)
                .withCloneAllBranches(this.cloneAllBranches)
                .withCloneSubmodules(this.cloneSubmodules)
                .withInitialBranch(this.initialBranch)
                .withGitDirPath(this.gitDirPath)
                .withMirror(this.mirror)
                .withRemote(this.remote)
                .withInstallAsContext(this.installAsContext)
                .withNoCheckout(this.noCheckout)
                .withTags(this.tags)
                .withTargetDir(this.targetDir)
                .withRepoUri(this.repoUri);
    }

    @Override
    public String toString() {
        return "clone{" 
                + "installAsContext=" + installAsContext 
                + ", bare=" + bare 
                + ", initialBranch='" + initialBranch + '\'' 
                + ", remote='" + remote + '\'' 
                + ", gitDirPath='" + gitDirPath + '\'' 
                + ", cloneAllBranches=" + cloneAllBranches 
                + ", branchesToClone=" + branchesToClone 
                + ", cloneSubmodules=" + cloneSubmodules 
                + ", depth=" + depth 
                + ", mirror=" + mirror 
                + ", targetDir='" + targetDir + '\'' 
                + ", noCheckout=" + noCheckout 
                + ", tags=" + tags 
                + ", repoUri='" + repoUri + '\'' 
                + '}';
    }

    public static CloneCommandBuilder builder() {
        return new CloneCommandBuilder();
    }

    public static final class CloneCommandBuilder implements Builder<CloneCommandBuilder, String, AutoGitCloneCommand> {

        private boolean installAsContext = DEFAULT_INSTALL_AS_CTX;
        private boolean bare;
        private String initialBranch;
        private String remote;
        private String gitDirPath;
        private boolean cloneAllBranches;
        private Set<String> branchesToClone;
        private boolean cloneSubmodules;
        private int depth;
        private boolean mirror;
        private String targetDir;
        private boolean noCheckout = DEFAULT_NO_CHECKOUT;
        private TagOpt tags;
        private String repoUri;

        public CloneCommandBuilder withInstallAsContext(final boolean installAsContext) {
            this.installAsContext = installAsContext;
            return this;
        }

        public CloneCommandBuilder withBare(final boolean bare) {
            this.bare = bare;
            return this;
        }

        public CloneCommandBuilder withInitialBranch(final String initialBranch) {
            this.initialBranch = initialBranch;
            return this;
        }

        public CloneCommandBuilder withRemote(final String remote) {
            this.remote = remote;
            return this;
        }

        public CloneCommandBuilder withGitDirPath(final String gitDirPath) {
            this.gitDirPath = gitDirPath;
            return this;
        }

        public CloneCommandBuilder withCloneAllBranches(final boolean cloneAllBranches) {
            this.cloneAllBranches = cloneAllBranches;
            return this;
        }

        public CloneCommandBuilder withBranchesToClone(final Set<String> branchesToClone) {
            this.branchesToClone = branchesToClone;
            return this;
        }

        public CloneCommandBuilder withCloneSubmodules(final boolean cloneSubmodules) {
            this.cloneSubmodules = cloneSubmodules;
            return this;
        }

        public CloneCommandBuilder withDepth(final int depth) {
            this.depth = depth;
            return this;
        }

        public CloneCommandBuilder withMirror(final boolean mirror) {
            this.mirror = mirror;
            return this;
        }

        public CloneCommandBuilder withTargetDir(final String targetDir) {
            this.targetDir = targetDir;
            return this;
        }

        public CloneCommandBuilder withNoCheckout(final boolean noCheckout) {
            this.noCheckout = noCheckout;
            return this;
        }

        public CloneCommandBuilder withTags(final TagOpt tags) {
            this.tags = tags;
            return this;
        }

        public CloneCommandBuilder withRepoUri(final String repoUri) {
            this.repoUri = repoUri;
            return this;
        }

        @Override
        public AutoGitCloneCommand build() {
            return new AutoGitCloneCommand(
                    this.installAsContext,
                    this.bare,
                    this.initialBranch,
                    this.remote,
                    this.gitDirPath,
                    this.cloneAllBranches,
                    this.branchesToClone,
                    this.cloneSubmodules,
                    this.depth,
                    this.mirror,
                    this.targetDir,
                    this.noCheckout,
                    this.tags,
                    this.repoUri
            );
        }
    }
}
