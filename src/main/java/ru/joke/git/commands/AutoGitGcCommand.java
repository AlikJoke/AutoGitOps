package ru.joke.git.commands;

import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@ClassPathIndexed("gc")
public final class AutoGitGcCommand implements AutoGitCommand<Properties, AutoGitGcCommand, AutoGitGcCommand.GcCommandBuilder> {

    private static final boolean DEFAULT_AGGRESSIVE = true;
    private static final boolean DEFAULT_PRUNE_PRESERVED = true;
    private static final boolean DEFAULT_PACK_KEPT_OBJECTS = true;
    private static final boolean DEFAULT_PRESERVE_OLD_PACKS = true;
    private static final long DEFAULT_EXPIRE_AFTER = 14;
    
    private final boolean aggressive;
    private final boolean prunePreserved;
    private final boolean packKeptObjects;
    private final boolean preserveOldPacks;
    private final long expireAfter;

    private AutoGitGcCommand() {
        this(
                DEFAULT_AGGRESSIVE,
                DEFAULT_PRUNE_PRESERVED,
                DEFAULT_PACK_KEPT_OBJECTS,
                DEFAULT_PRESERVE_OLD_PACKS,
                DEFAULT_EXPIRE_AFTER
        );
    }

    private AutoGitGcCommand(
            final boolean aggressive,
            final boolean prunePreserved,
            final boolean packKeptObjects,
            final boolean preserveOldPacks,
            final long expireAfter
    ) {
        this.aggressive = aggressive;
        this.prunePreserved = prunePreserved;
        this.packKeptObjects = packKeptObjects;
        this.preserveOldPacks = preserveOldPacks;
        this.expireAfter = expireAfter;
    }

    @Override
    public Properties call() {

        try {
            final var git = GitStorage.getGit();
            final var gcCommand = git.gc();

            if (this.expireAfter > 0) {
                gcCommand.setExpire(Instant.now().minus(this.expireAfter, TimeUnit.DAYS.toChronoUnit()));
            }

            return gcCommand
                    .setAggressive(this.aggressive)
                    .setPrunePreserved(this.prunePreserved)
                    .setPackKeptObjects(this.packKeptObjects)
                    .setPreserveOldPacks(this.preserveOldPacks)
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GcCommandBuilder toBuilder() {
        return builder()
                .withAggressive(this.aggressive)
                .withExpireAfter(this.expireAfter)
                .withPackKeptObjects(this.packKeptObjects)
                .withPrunePreserved(this.prunePreserved)
                .withPreserveOldPacks(this.preserveOldPacks);
    }

    @Override
    public String toString() {
        return "gc{"
                + "aggressive=" + aggressive
                + ", prunePreserved=" + prunePreserved
                + ", packKeptObjects=" + packKeptObjects
                + ", preserveOldPacks=" + preserveOldPacks
                + ", expireAfter=" + expireAfter
                + '}';
    }

    public static GcCommandBuilder builder() {
        return new GcCommandBuilder();
    }

    public static final class GcCommandBuilder implements Builder<GcCommandBuilder, Properties, AutoGitGcCommand> {

        private boolean aggressive = DEFAULT_AGGRESSIVE;
        private boolean prunePreserved = DEFAULT_PRUNE_PRESERVED;
        private boolean packKeptObjects = DEFAULT_PACK_KEPT_OBJECTS;
        private boolean preserveOldPacks = DEFAULT_PRESERVE_OLD_PACKS;
        private long expireAfter = DEFAULT_EXPIRE_AFTER;

        public GcCommandBuilder withAggressive(final boolean aggressive) {
            this.aggressive = aggressive;
            return this;
        }

        public GcCommandBuilder withPrunePreserved(final boolean prunePreserved) {
            this.prunePreserved = prunePreserved;
            return this;
        }

        public GcCommandBuilder withPackKeptObjects(final boolean packKeptObjects) {
            this.packKeptObjects = packKeptObjects;
            return this;
        }

        public GcCommandBuilder withPreserveOldPacks(final boolean preserveOldPacks) {
            this.preserveOldPacks = preserveOldPacks;
            return this;
        }

        public GcCommandBuilder withExpireAfter(final long expireAfter) {
            this.expireAfter = expireAfter;
            return this;
        }

        @Override
        public AutoGitGcCommand build() {
            return new AutoGitGcCommand(
                    this.aggressive,
                    this.prunePreserved,
                    this.packKeptObjects,
                    this.preserveOldPacks,
                    this.expireAfter
            );
        }
    }
}
