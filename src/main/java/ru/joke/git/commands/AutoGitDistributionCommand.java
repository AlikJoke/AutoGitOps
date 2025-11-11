package ru.joke.git.commands;

import org.eclipse.jgit.api.CheckoutResult;
import org.eclipse.jgit.api.CherryPickResult;
import ru.joke.classpath.ClassPathIndexed;

import java.util.List;

@ClassPathIndexed("distribute")
public final class AutoGitDistributionCommand implements AutoGitCommand<String> {

    private final List<String> branches;
    private final AutoGitCheckoutCommand checkout;
    private final AutoGitPullCommand pull;
    private final AutoGitCherryPickCommand cherryPick;
    private final AutoGitPushCommand push;

    private AutoGitDistributionCommand(
            final List<String> branches,
            final AutoGitCheckoutCommand checkout,
            final AutoGitPullCommand pull,
            final AutoGitCherryPickCommand cherryPick,
            final AutoGitPushCommand push
    ) {
        this.branches = branches;
        this.checkout = checkout == null ? new AutoGitCheckoutCommand() : checkout;
        this.pull = pull;
        this.cherryPick = cherryPick;
        this.push = push;
    }

    @Override
    public String call() {
        for (final var branch : this.branches) {

            this.checkout.setBranch(branch);

            final var checkoutResult = this.checkout.call();
            if (checkoutResult != CheckoutResult.Status.OK) {
                return checkoutResult.toString();
            }

            String result;
            try {
                result = executeActionInBranch();
            } catch (RuntimeException ex) {
                result = executeActionInBranch();
            }

            if (result != null) {
                return result;
            }
        }

        return "OK";
    }

    private String executeActionInBranch() {

        this.pull.setBranch(null);

        final var pullResult = this.pull.call();
        if (!pullResult.isSuccessful()) {
            return pullResult.toString();
        }

        final var cherryPickResult = this.cherryPick.call();
        if (cherryPickResult.getStatus() != CherryPickResult.CherryPickStatus.OK) {
            return cherryPickResult.toString();
        }

        this.push.call();

        return null;
    }
}
