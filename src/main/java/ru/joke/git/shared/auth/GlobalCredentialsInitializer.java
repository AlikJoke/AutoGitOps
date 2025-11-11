package ru.joke.git.shared.auth;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public final class GlobalCredentialsInitializer {

    public void initialize(
            final String username,
            final String password
    ) {
        final CredentialsProvider provider = new UsernamePasswordCredentialsProvider(username, password);
        CredentialsProvider.setDefault(provider);
    }
}
