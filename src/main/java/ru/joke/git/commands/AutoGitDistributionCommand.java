package ru.joke.git.commands;

import org.eclipse.jgit.api.CheckoutResult;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import ru.joke.classpath.ClassPathIndexed;

import java.util.*;
import java.util.stream.Collectors;

@ClassPathIndexed("distribute")
public final class AutoGitDistributionCommand implements AutoGitCommand<Map<String, AutoGitDistributionCommand.BranchPublicationResult>, AutoGitDistributionCommand, AutoGitDistributionCommand.DistributionCommandBuilder> {

    private static final String INITIAL_BRANCH = "$initial";

    private final List<String> branches;
    private final AutoGitCheckoutCommand checkout;
    private final AutoGitPullCommand pull;
    private final AutoGitCherryPickCommand cherryPick;
    private final AutoGitPushCommand push;
    private final AutoGitAddCommand add;
    private final AutoGitCommitCommand commit;

    private AutoGitDistributionCommand() {
        final var checkoutCommand = AutoGitCheckoutCommand.builder().build();
        final var pullCommand = AutoGitPullCommand.builder().build();
        final var pushCommand = AutoGitPushCommand.builder().build();
        final var addCommand = AutoGitAddCommand.builder().build();
        this(
                null,
                checkoutCommand,
                pullCommand,
                null,
                pushCommand,
                addCommand,
                null
        );
    }
    
    private AutoGitDistributionCommand(
            final List<String> branches,
            final AutoGitCheckoutCommand checkout,
            final AutoGitPullCommand pull,
            final AutoGitCherryPickCommand cherryPick,
            final AutoGitPushCommand push,
            final AutoGitAddCommand add,
            final AutoGitCommitCommand commit
    ) {
        this.branches = branches;
        this.checkout = checkout;
        this.pull = pull;
        this.cherryPick = cherryPick;
        this.push = push;
        this.add = add;
        this.commit = commit;
    }

    @Override
    public Map<String, BranchPublicationResult> call() {
        if ((this.cherryPick == null || this.cherryPick.getCommitHashes().isEmpty()) && this.commit == null) {
            throw new IllegalStateException("Commit refs to distribution is required");
        }

        final Map<String, BranchPublicationResult> result = new HashMap<>();

        final var pullCommand = buildPullCommand();
        final var cherryPickCommand =
                this.commit != null
                        ? createInitialPublication(pullCommand, result)
                        : this.cherryPick;

        final var checkoutCommandBuilder = this.checkout.toBuilder();
        for (final var branch : this.branches) {

            final var branchPublicationResult = result.computeIfAbsent(branch, k -> new BranchPublicationResult());
            final var checkoutCommand = buildCheckoutCommand(branch, checkoutCommandBuilder);
            final var checkoutResult = checkoutCommand.call();

            if (checkoutResult.getStatus() != CheckoutResult.Status.OK) {
                branchPublicationResult.failedCheckout = checkoutResult;
                continue;
            }

            try {
                executeActionInBranch(
                        branch,
                        cherryPickCommand,
                        pullCommand,
                        branchPublicationResult
                );
            } catch (RuntimeException ex) {
                executeActionInBranch(
                        branch,
                        cherryPickCommand,
                        pullCommand,
                        branchPublicationResult
                );
            }
        }

        return result;
    }

    @Override
    public DistributionCommandBuilder toBuilder() {
        return builder()
                .withAdd(this.add)
                .withCheckout(this.checkout)
                .withBranches(this.branches)
                .withCherryPick(this.cherryPick)
                .withPull(this.pull)
                .withCommit(this.commit)
                .withPush(this.push);
    }

    @Override
    public String toString() {
        return "distribute{"
                + "branches=" + branches
                + ", checkout=" + checkout
                + ", pull=" + pull
                + ", cherryPick=" + cherryPick
                + ", push=" + push
                + ", add=" + add
                + ", commit=" + commit
                + '}';
    }

    private AutoGitCheckoutCommand buildCheckoutCommand(
            final String branch,
            final AutoGitCheckoutCommand.CheckoutCommandBuilder builder
    ) {
        return builder
                    .withBranch(branch)
                    .withForceRefUpdate(true)
                .build();
    }

    private AutoGitPullCommand buildPullCommand() {
        final var pullCommandBuilder =
                this.pull == null
                        ? AutoGitPullCommand.builder()
                        : this.pull.toBuilder().withBranch(null);
        return pullCommandBuilder.build();
    }

    private AutoGitCherryPickCommand createInitialPublication(
            final AutoGitPullCommand pullCommand,
            final Map<String, BranchPublicationResult> result
    ) {
        final AutoGitPublishCommand publishCommand =
                AutoGitPublishCommand.builder()
                            .withAdd(this.add)
                            .withCommit(this.commit)
                            .withPush(this.push)
                            .withPull(pullCommand)
                        .build();

        final var commitId = publishCommand.call();
        final var branchPublicationResult = result.computeIfAbsent(INITIAL_BRANCH, k -> new BranchPublicationResult());
        branchPublicationResult.pushedCommits = Set.of(commitId);

        final var cherryPickCommandBuilder =
                this.cherryPick == null
                        ? AutoGitCherryPickCommand.builder()
                        : this.cherryPick.toBuilder();
        return cherryPickCommandBuilder
                    .withCommitHash(commitId)
                    .withNoCommit(false)
                .build();
    }

    private void executeActionInBranch(
            final String branch,
            final AutoGitCherryPickCommand cherryPickCommand,
            final AutoGitPullCommand pullCommand,
            final BranchPublicationResult branchPublicationResult
    ) {

        final var pullResult = pullCommand.call();
        if (!pullResult.isSuccessful()) {
            branchPublicationResult.failedPull = pullResult;
            return;
        }

        final var cherryPickResult = cherryPickCommand.call();
        if (cherryPickResult.getStatus() != CherryPickResult.CherryPickStatus.OK) {
            branchPublicationResult.failedCherryPick = cherryPickResult;
            return;
        }

        this.push.call();

        branchPublicationResult.pushedCommits =
                cherryPickResult.getCherryPickedRefs()
                        .stream()
                        .map(Ref::getObjectId)
                        .map(AnyObjectId::getName)
                        .collect(Collectors.toSet());
    }

    public static AutoGitDistributionCommand.DistributionCommandBuilder builder() {
        return new DistributionCommandBuilder();
    }

    public static class BranchPublicationResult {
        private CheckoutResult failedCheckout;
        private CherryPickResult failedCherryPick;
        private PullResult failedPull;
        private Set<String> pushedCommits;
    }

    public static final class DistributionCommandBuilder implements Builder<AutoGitDistributionCommand.DistributionCommandBuilder, Map<String, BranchPublicationResult>, AutoGitDistributionCommand> {

        private final List<String> branches = new ArrayList<>();
        private AutoGitCheckoutCommand checkout = AutoGitCheckoutCommand.builder().build();
        private AutoGitPullCommand pull = AutoGitPullCommand.builder().build();
        private AutoGitCherryPickCommand cherryPick;
        private AutoGitPushCommand push = AutoGitPushCommand.builder().build();
        private AutoGitAddCommand add = AutoGitAddCommand.builder().build();
        private AutoGitCommitCommand commit;

        public DistributionCommandBuilder withCheckout(final AutoGitCheckoutCommand checkout) {
            this.checkout = checkout;
            return this;
        }

        public DistributionCommandBuilder withPull(final AutoGitPullCommand pull) {
            this.pull = pull;
            return this;
        }

        public DistributionCommandBuilder withCherryPick(final AutoGitCherryPickCommand cherryPick) {
            this.cherryPick = cherryPick;
            return this;
        }

        public DistributionCommandBuilder withPush(final AutoGitPushCommand push) {
            this.push = push;
            return this;
        }

        public DistributionCommandBuilder withAdd(final AutoGitAddCommand add) {
            this.add = add;
            return this;
        }

        public DistributionCommandBuilder withCommit(final AutoGitCommitCommand commit) {
            this.commit = commit;
            return this;
        }

        public DistributionCommandBuilder withBranch(final String branch) {
            this.branches.add(branch);
            return this;
        }

        public DistributionCommandBuilder withBranches(final List<String> branches) {
            this.branches.addAll(branches);
            return this;
        }

        @Override
        public AutoGitDistributionCommand build() {
            return new AutoGitDistributionCommand(
                    this.branches,
                    this.checkout,
                    this.pull,
                    this.cherryPick,
                    this.push,
                    this.add,
                    this.commit
            );
        }
    }
}
