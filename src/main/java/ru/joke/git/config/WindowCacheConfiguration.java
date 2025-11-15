package ru.joke.git.config;

import org.eclipse.jgit.storage.file.WindowCacheConfig;

public final class WindowCacheConfiguration {

    private static final int DEFAULT_DELTA_BASE_CACHE_LIMIT = 512 * WindowCacheConfig.MB;
    private static final long DEFAULT_FILE_DATA_CACHE_LIMIT = 1024 * WindowCacheConfig.MB;
    private static final int DEFAULT_WINDOW_SIZE = 512 * WindowCacheConfig.MB;
    private static final int DEFAULT_OPEN_FILES_LIMIT = 512;
    private static final boolean DEFAULT_USE_MMAP = true;

    private final int deltaBaseCacheLimitBytes;
    private final long packedFileDataCacheLimit;
    private final int packedWindowSizeBytes;
    private final int packedOpenFilesLimit;
    private final boolean packedUseMmap;

    public WindowCacheConfiguration() {
        this(
                DEFAULT_DELTA_BASE_CACHE_LIMIT,
                DEFAULT_FILE_DATA_CACHE_LIMIT,
                DEFAULT_WINDOW_SIZE,
                DEFAULT_OPEN_FILES_LIMIT,
                DEFAULT_USE_MMAP
        );
    }

    public WindowCacheConfiguration(
            final int deltaBaseCacheLimitBytes,
            final long packedFileDataCacheLimit,
            final int packedWindowSizeBytes,
            final int packedOpenFilesLimit,
            final boolean packedUseMmap
    ) {
        this.deltaBaseCacheLimitBytes = deltaBaseCacheLimitBytes;
        this.packedFileDataCacheLimit = packedFileDataCacheLimit;
        this.packedWindowSizeBytes = packedWindowSizeBytes;
        this.packedOpenFilesLimit = packedOpenFilesLimit;
        this.packedUseMmap = packedUseMmap;
    }

    public void configure() {
        final var cfg = new WindowCacheConfig();

        cfg.setDeltaBaseCacheLimit(this.deltaBaseCacheLimitBytes);

        cfg.setPackedGitLimit(this.packedFileDataCacheLimit);
        cfg.setPackedGitWindowSize(this.packedWindowSizeBytes);
        cfg.setPackedGitOpenFiles(this.packedOpenFilesLimit);
        cfg.setPackedGitMMAP(this.packedUseMmap);

        cfg.install();
    }
}
