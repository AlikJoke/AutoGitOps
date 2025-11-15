package ru.joke.git.shared.auth;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public final class GlobalCredentialsInitializer {

    public void initialize(
            final String username,
            final char[] password
    ) {
        if (password.length == 0) {
            throw new IllegalArgumentException("Non-empty password is required");
        }

        final var provider = new UsernamePasswordCredentialsProvider(username, password);
        CredentialsProvider.setDefault(provider);
    }
}
