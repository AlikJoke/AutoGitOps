package ru.joke.git.commands;

import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;

import java.util.Properties;

@ClassPathIndexed("optimize")
public final class AutoGitOptimizeCommand implements AutoGitCommand<AutoGitOptimizeCommand.OptimizationResult, AutoGitOptimizeCommand, AutoGitOptimizeCommand.OptimizeCommandBuilder> {

    private final AutoGitPackRefsCommand packRefs;
    private final AutoGitGcCommand gc;

    private AutoGitOptimizeCommand() {
        final var packRefs = AutoGitPackRefsCommand.builder().build();
        final var gc = AutoGitGcCommand.builder().build();
        this(gc, packRefs);
    }

    private AutoGitOptimizeCommand(
            final AutoGitGcCommand gc,
            final AutoGitPackRefsCommand packRefs
    ) {
        this.gc = gc;
        this.packRefs = packRefs;
    }

    @Override
    public OptimizationResult call() {
        final var git = GitStorage.getGit();

        Properties gcResult = null;
        if (this.gc != null) {
            gcResult = this.gc.call();
        }

        String packRefsResult = null;
        if (this.packRefs != null) {
            packRefsResult = this.packRefs.call();
        }

        return new OptimizationResult(packRefsResult, gcResult);
    }

    @Override
    public OptimizeCommandBuilder toBuilder() {
        return builder()
                .withGc(this.gc)
                .withPackRefs(this.packRefs);
    }

    @Override
    public String toString() {
        return "optimize{"
                + "packRefs=" + packRefs
                + ", gc=" + gc
                + '}';
    }

    public static OptimizeCommandBuilder builder() {
        return new OptimizeCommandBuilder();
    }

    public record OptimizationResult(
            String packRefsResult,
            Properties gcResult
    ) {}

    public static final class OptimizeCommandBuilder implements Builder<OptimizeCommandBuilder, OptimizationResult, AutoGitOptimizeCommand> {

        private AutoGitPackRefsCommand packRefs = AutoGitPackRefsCommand.builder().build();
        private AutoGitGcCommand gc = AutoGitGcCommand.builder().build();

        public OptimizeCommandBuilder withPackRefs(final AutoGitPackRefsCommand packRefs) {
            this.packRefs = packRefs;
            return this;
        }

        public OptimizeCommandBuilder withGc(final AutoGitGcCommand gc) {
            this.gc = gc;
            return this;
        }

        @Override
        public AutoGitOptimizeCommand build() {
            return new AutoGitOptimizeCommand(this.gc, this.packRefs);
        }
    }
}
