package ru.joke.git.commands;

import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.ProgressMonitorStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ClassPathIndexed("reset")
public final class AutoGitResetCommand implements AutoGitCommand<String, AutoGitResetCommand, AutoGitResetCommand.ResetCommandBuilder> {

    private static final ResetCommand.ResetType DEFAULT_RESET_MODE = ResetCommand.ResetType.SOFT;

    private final boolean disableRefLog;
    private final String ref;
    private final ResetCommand.ResetType resetMode;
    private final List<String> files;

    private AutoGitResetCommand() {
        this(
                false, 
                null,
                DEFAULT_RESET_MODE,
                null
        );
    }

    private AutoGitResetCommand(
            final boolean disableRefLog,
            final String ref,
            final ResetCommand.ResetType resetMode,
            final List<String> files
    ) {
        this.disableRefLog = disableRefLog;
        this.files = files == null ? new ArrayList<>() : files;
        this.resetMode = resetMode;
        this.ref = ref;
    }

    @Override
    public String call() {
        final var resetCommand = GitStorage.getGit().reset();
        try {
            if (this.files != null) {
                this.files.forEach(resetCommand::addPath);
            }
            
            return resetCommand
                    .setProgressMonitor(ProgressMonitorStorage.getProgressMonitor())
                    .setMode(this.resetMode)
                    .setRef(this.ref)
                    .disableRefLog(this.disableRefLog)
                    .call()
                    .getName();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResetCommandBuilder toBuilder() {
        return builder()
                .withRef(this.ref)
                .withFilesToReset(this.files == null ? Collections.emptyList() : this.files)
                .withDisableRefLog(this.disableRefLog)
                .withResetMode(this.resetMode);
    }

    @Override
    public String toString() {
        return "reset{" 
                + "disableRefLog=" + disableRefLog 
                + ", ref='" + ref + '\'' 
                + ", resetMode=" + resetMode 
                + ", files=" + files 
                + '}';
    }

    public static ResetCommandBuilder builder() {
        return new ResetCommandBuilder();
    }

    public static final class ResetCommandBuilder implements Builder<ResetCommandBuilder, String, AutoGitResetCommand> {

        private boolean disableRefLog;
        private String ref;
        private ResetCommand.ResetType resetMode = DEFAULT_RESET_MODE;
        private List<String> files;

        public ResetCommandBuilder withDisableRefLog(final boolean disableRefLog) {
            this.disableRefLog = disableRefLog;
            return this;
        }

        public ResetCommandBuilder withRef(final String ref) {
            this.ref = ref;
            return this;
        }

        public ResetCommandBuilder withResetMode(final ResetCommand.ResetType resetMode) {
            this.resetMode = resetMode;
            return this;
        }

        public ResetCommandBuilder withFileToReset(final String file) {
            this.files.add(file);
            return this;
        }

        public ResetCommandBuilder withFilesToReset(final List<String> files) {
            this.files.addAll(files);
            return this;
        }

        @Override
        public AutoGitResetCommand build() {
            return new AutoGitResetCommand(
                    this.disableRefLog,
                    this.ref,
                    this.resetMode,
                    this.files
            );
        }
    }
}
