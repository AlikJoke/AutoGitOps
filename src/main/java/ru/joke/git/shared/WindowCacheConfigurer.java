package ru.joke.git.shared;

import org.eclipse.jgit.storage.file.WindowCacheConfig;

import java.util.Map;

public final class WindowCacheConfigurer {

    private static final String DELTA_BASE_CACHE_LIMIT_BYTES = "delta.base.cache.limit.bytes";
    private static final String PACKED_FILE_DATA_CACHE_LIMIT = "packed.file.data.cache.limit.bytes";
    private static final String PACKED_WINDOW_SIZE = "packed.window.size.bytes";
    private static final String PACKED_OPEN_FILES = "packed.open.files.limit";
    private static final String PACKED_USE_MMAP = "packed.use.mmap";

    private static final int DEFAULT_DELTA_BASE_CACHE_LIMIT = 512 * WindowCacheConfig.MB;
    private static final long DEFAULT_FILE_DATA_CACHE_LIMIT = 1024 * WindowCacheConfig.MB;
    private static final int DEFAULT_WINDOW_SIZE = 512 * WindowCacheConfig.MB;
    private static final int DEFAULT_OPEN_FILES_LIMIT = 512;

    public void configure(final Map<String, String> properties) {
        final var cfg = new WindowCacheConfig();

        final int deltaBaseCacheLimit = getPropertyValue(properties, DELTA_BASE_CACHE_LIMIT_BYTES, DEFAULT_DELTA_BASE_CACHE_LIMIT);
        cfg.setDeltaBaseCacheLimit(deltaBaseCacheLimit);

        final long fileDataCacheLimit = getPropertyValue(properties, PACKED_FILE_DATA_CACHE_LIMIT, DEFAULT_FILE_DATA_CACHE_LIMIT);
        cfg.setPackedGitLimit(fileDataCacheLimit);

        final int windowSize = getPropertyValue(properties, PACKED_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
        cfg.setPackedGitWindowSize(windowSize);

        final int openFilesLimit = getPropertyValue(properties, PACKED_OPEN_FILES, DEFAULT_OPEN_FILES_LIMIT);
        cfg.setPackedGitOpenFiles(openFilesLimit);

        final var useMmap = properties.get(PACKED_USE_MMAP) == null || Boolean.parseBoolean(properties.get(PACKED_USE_MMAP));
        cfg.setPackedGitMMAP(useMmap);

        cfg.install();
    }

    private long getPropertyValue(
            final Map<String, String> properties,
            final String propertyName,
            final long defaultValue
    ) {
        final var propertyValue = properties.get(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }

        return Long.parseLong(propertyValue);
    }

    private int getPropertyValue(
            final Map<String, String> properties,
            final String propertyName,
            final int defaultValue
    ) {
        final var propertyValue = properties.get(propertyName);
        if (propertyValue == null) {
            return defaultValue;
        }

        return Integer.parseInt(propertyValue);
    }
}
