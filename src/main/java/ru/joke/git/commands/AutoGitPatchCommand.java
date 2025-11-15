package ru.joke.git.commands;

import org.eclipse.jgit.api.CheckoutResult;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.ResetCommand;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

@ClassPathIndexed("patch")
public final class AutoGitPatchCommand implements AutoGitCommand<AutoGitPatchCommand.PatchResult, AutoGitPatchCommand, AutoGitPatchCommand.PatchCommandBuilder> {

    private final AutoGitResetCommand reset;
    private final AutoGitCheckoutCommand checkout;
    private final AutoGitCherryPickCommand cherryPick;

    private AutoGitPatchCommand() {
        final var resetCommand =
                AutoGitResetCommand.builder()
                            .withResetMode(ResetCommand.ResetType.HARD)
                        .build();
        this(resetCommand, null, null);
    }

    private AutoGitPatchCommand(
            final AutoGitResetCommand reset,
            final AutoGitCheckoutCommand checkout,
            final AutoGitCherryPickCommand cherryPick
    ) {
        this.reset = reset;
        this.checkout = checkout;
        this.cherryPick = cherryPick;
    }

    @Override
    public PatchResult call() {
        if (this.checkout == null) {
            throw new IllegalStateException("Checkout is required for patch command");
        }
        if (this.cherryPick == null) {
            throw new IllegalStateException("Cherry-pick is required for patch command");
        }
        
        final var git = GitStorage.getGit();

        if (this.reset != null) {
            this.reset.call();
        }

        final var checkoutResult = this.checkout.call();
        if (checkoutResult.getStatus() != CheckoutResult.Status.OK) {
            return new PatchResult(checkoutResult, null);
        }

        final var cherryPickResult = this.cherryPick.call();
        return new PatchResult(checkoutResult, cherryPickResult);
    }

    @Override
    public PatchCommandBuilder toBuilder() {
        return builder()
                .withReset(this.reset)
                .withCheckout(this.checkout)
                .withCherryPick(this.cherryPick);
    }

    @Override
    public String toString() {
        return "patch{"
                + "checkout=" + checkout
                + ", cherryPick=" + cherryPick
                + ", reset=" + reset
                + '}';
    }

    public static PatchCommandBuilder builder() {
        return new PatchCommandBuilder();
    }

    public record PatchResult(
            CheckoutResult checkoutResult,
            CherryPickResult cherryPickResult
    ) {}

    public static final class PatchCommandBuilder implements Builder<PatchCommandBuilder, PatchResult, AutoGitPatchCommand> {

        private AutoGitResetCommand reset =
                AutoGitResetCommand.builder()
                            .withResetMode(ResetCommand.ResetType.HARD)
                        .build();
        private AutoGitCheckoutCommand checkout;
        private AutoGitCherryPickCommand cherryPick;

        public PatchCommandBuilder withReset(final AutoGitResetCommand reset) {
            this.reset = reset;
            return this;
        }

        public PatchCommandBuilder withCheckout(final AutoGitCheckoutCommand checkout) {
            this.checkout = checkout;
            return this;
        }

        public PatchCommandBuilder withCherryPick(final AutoGitCherryPickCommand cherryPick) {
            this.cherryPick = cherryPick;
            return this;
        }

        @Override
        public AutoGitPatchCommand build() {
            return new AutoGitPatchCommand(
                    this.reset,
                    this.checkout,
                    this.cherryPick
            );
        }
    }
}
