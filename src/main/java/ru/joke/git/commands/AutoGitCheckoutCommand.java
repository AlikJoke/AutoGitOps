package ru.joke.git.commands;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CheckoutResult;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

@ClassPathIndexed("checkout")
public final class AutoGitCheckoutCommand implements AutoGitCommand<CheckoutResult, AutoGitCheckoutCommand, AutoGitCheckoutCommand.CheckoutCommandBuilder> {

    private final boolean forced;
    private final boolean createBranch;
    private final boolean orphan;
    private final boolean forceRefUpdate;
    private final String ref;
    private final CheckoutCommand.Stage stage;
    private final CreateBranchCommand.SetupUpstreamMode upstreamMode;
    private final String startPoint;

    private AutoGitCheckoutCommand() {
        this(
                false,
                false,
                false,
                false,
                null,
                null,
                null,
                null
        );
    }

    private AutoGitCheckoutCommand(
            final boolean forced,
            final boolean createBranch,
            final boolean orphan,
            final boolean forceRefUpdate,
            final String ref,
            final CheckoutCommand.Stage stage,
            final CreateBranchCommand.SetupUpstreamMode upstreamMode,
            final String startPoint
    ) {
        this.forced = forced;
        this.createBranch = createBranch;
        this.orphan = orphan;
        this.forceRefUpdate = forceRefUpdate;
        this.ref = ref;
        this.stage = stage;
        this.upstreamMode = upstreamMode;
        this.startPoint = startPoint;
    }

    @Override
    public CheckoutResult call() {
        if (this.ref == null || this.ref.isBlank()) {
            throw new IllegalStateException("Ref to checkout is required for checkout command");
        }

        final var checkoutCommand = GitStorage.getGit().checkout();
        try {
            checkoutCommand
                    .setForced(this.forced)
                    .setCreateBranch(this.createBranch)
                    .setOrphan(this.orphan)
                    .setForceRefUpdate(this.forceRefUpdate)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setName(this.ref)
                    .setStage(this.stage)
                    .setUpstreamMode(this.upstreamMode)
                    .setStartPoint(this.startPoint)
                    .call();
            return checkoutCommand.getResult();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CheckoutCommandBuilder toBuilder() {
        return builder()
                .withRef(this.ref)
                .withForced(this.forced)
                .withOrphan(this.orphan)
                .withStage(this.stage)
                .withStartPoint(this.startPoint)
                .withForceRefUpdate(this.forceRefUpdate)
                .withCreateBranch(this.createBranch)
                .withUpstreamMode(this.upstreamMode);
    }

    @Override
    public String toString() {
        return "checkout{"
                + "forced=" + forced
                + ", createBranch=" + createBranch
                + ", orphan=" + orphan
                + ", forceRefUpdate=" + forceRefUpdate
                + ", ref='" + ref + '\''
                + ", stage=" + stage
                + ", upstreamMode=" + upstreamMode
                + ", startPoint='" + startPoint + '\''
                + '}';
    }

    public static CheckoutCommandBuilder builder() {
        return new CheckoutCommandBuilder();
    }

    public static final class CheckoutCommandBuilder implements Builder<CheckoutCommandBuilder, CheckoutResult, AutoGitCheckoutCommand> {

        private boolean forced;
        private boolean createBranch;
        private boolean orphan;
        private boolean forceRefUpdate;
        private String ref;
        private CheckoutCommand.Stage stage;
        private CreateBranchCommand.SetupUpstreamMode upstreamMode;
        private String startPoint;

        public CheckoutCommandBuilder withForced(final boolean forced) {
            this.forced = forced;
            return this;
        }

        public CheckoutCommandBuilder withCreateBranch(final boolean createBranch) {
            this.createBranch = createBranch;
            return this;
        }

        public CheckoutCommandBuilder withOrphan(final boolean orphan) {
            this.orphan = orphan;
            return this;
        }

        public CheckoutCommandBuilder withForceRefUpdate(final boolean forceRefUpdate) {
            this.forceRefUpdate = forceRefUpdate;
            return this;
        }

        public CheckoutCommandBuilder withRef(final String ref) {
            this.ref = ref;
            return this;
        }

        public CheckoutCommandBuilder withStage(final CheckoutCommand.Stage stage) {
            this.stage = stage;
            return this;
        }

        public CheckoutCommandBuilder withUpstreamMode(final CreateBranchCommand.SetupUpstreamMode upstreamMode) {
            this.upstreamMode = upstreamMode;
            return this;
        }

        public CheckoutCommandBuilder withStartPoint(final String startPoint) {
            this.startPoint = startPoint;
            return this;
        }

        @Override
        public AutoGitCheckoutCommand build() {
            return new AutoGitCheckoutCommand(
                    this.forced,
                    this.createBranch,
                    this.orphan,
                    this.forceRefUpdate,
                    this.ref,
                    this.stage,
                    this.upstreamMode,
                    this.startPoint
            );
        }
    }
}
