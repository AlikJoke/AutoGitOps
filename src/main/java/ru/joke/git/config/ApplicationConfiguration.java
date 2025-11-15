package ru.joke.git.config;

public record ApplicationConfiguration(
        String repoPath,
        Auth auth,
        WindowCacheConfiguration windowCache
) {

    public ApplicationConfiguration(
            final String repoPath,
            final Auth auth,
            final WindowCacheConfiguration windowCache
    ) {
        this.repoPath = repoPath;
        this.auth = auth;
        this.windowCache = windowCache == null ? new WindowCacheConfiguration() : windowCache;
    }

    public record Auth(
            Credentials credentials,
            Ssh ssh
    ) {

        public record Credentials(
                String username,
                String password
        ) {
            @Override
            public String toString() {
                return "Credentials{"
                        + "username='" + username + '\''
                        + '}';
            }
        }

        public record Ssh(
                String pkFilePath,
                String passphrase,
                boolean useLegacyKex
        ) {
            @Override
            public String toString() {
                return "Ssh{"
                        + "pkFilePath='" + pkFilePath + '\''
                        + ", useLegacyKex=" + useLegacyKex
                        + '}';
            }
        }
    }
}
