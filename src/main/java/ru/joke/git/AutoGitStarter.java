package ru.joke.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import ru.joke.git.commands.AutoGitCommandFactory;
import ru.joke.git.config.ApplicationConfiguration;
import ru.joke.git.shared.*;
import ru.joke.git.shared.auth.GlobalCredentialsInitializer;
import ru.joke.git.shared.auth.SshdSessionFactoryInitializer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.IO.println;
import static java.lang.IO.readln;

abstract class AutoGitStarter {

    private static final String COMMAND_DELIMITER = "\\|\\|";
    private static final String PARAM_DELIMITER = "=";
    private static final String EXIT_COMMAND = "exit";

    private static final String AWAIT_NEXT_COMMAND_INFO = "Ready to next commands...";
    private static final String STARTED_INFO = "Started, ready to execute commands";
    private static final String STOPPED_INFO = "Successfully stopped";

    private static final String CONFIG_PARAM = "cfg";

    private static final JsonService jsonService = new JsonService();
    private static final AutoGitCommandFactory commandFactory = new AutoGitCommandFactory(jsonService);

    static void main(String[] args) throws GeneralSecurityException, IOException {

        initializeSharedResources(args);

        println(STARTED_INFO);

        while (!Thread.currentThread().isInterrupted()) {
            final var command = readln();
            if (command == null || EXIT_COMMAND.equalsIgnoreCase(command)) {
                break;
            }

            executeCommandNoEx(command);
        }

        println(STOPPED_INFO);
    }

    private static void executeCommandNoEx(final String parameters) {
        try {
            executeCommand(parameters);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

        println(AWAIT_NEXT_COMMAND_INFO);
    }

    private static void executeCommand(final String commandStr) {
        final var commands = commandStr.split(COMMAND_DELIMITER);
        for (var command : commands) {
            final var commandData = command.split(PARAM_DELIMITER, 2);
            println("Process command: " + command);

            final var cmd = commandFactory.create(commandData[0], commandData[1]);
            final var cmdResult = cmd.call();
            final var cmdResultAsJson = jsonService.serialize(cmdResult);

            println("Output:");
            println(cmdResultAsJson);
        }
    }

    private static Map<String, String> parseArgs(final String[] args) {
        return Arrays.stream(args)
                        .map(arg -> arg.split(PARAM_DELIMITER, 2))
                        .filter(arg -> arg.length > 1)
                        .collect(Collectors.toMap(arg -> arg[0], arg -> arg[1]));
    }

    private static void initializeSharedResources(final String[] args) throws GeneralSecurityException, IOException {

        final var argsMap = parseArgs(args);
        final var configJson = argsMap.get(CONFIG_PARAM);
        if (configJson == null) {
            throw new IllegalArgumentException("Config arg '%s' is required".formatted(CONFIG_PARAM));
        }

        final var config = jsonService.deserialize(configJson, ApplicationConfiguration.class);
        final var authConfig = config.auth();

        initSshConfig(authConfig);
        initDefaultCredentialsIfNeed(authConfig);

        if (config.repoPath() != null) {
            initGitStorage(config.repoPath());
        }

        initDefaultProgressMonitorStorage();
        config.windowCache().configure();
    }

    private static void initSshConfig(final ApplicationConfiguration.Auth authConfig) throws GeneralSecurityException, IOException {
        final var sshConfig = authConfig == null ? null : authConfig.ssh();

        if (sshConfig != null && sshConfig.pkFilePath() != null && !sshConfig.pkFilePath().isBlank()) {
            final var sshInitializer = new SshdSessionFactoryInitializer();
            sshInitializer.initialize(sshConfig.pkFilePath(), sshConfig.passphrase(), sshConfig.useLegacyKex());
        }
    }

    private static void initDefaultCredentialsIfNeed(final ApplicationConfiguration.Auth authConfig) {
        final var credentialsConfig = authConfig == null ? null : authConfig.credentials();

        if (credentialsConfig != null && credentialsConfig.username() != null && !credentialsConfig.username().isBlank()) {
            final var credentialsInitializer = new GlobalCredentialsInitializer();
            credentialsInitializer.initialize(credentialsConfig.username(), credentialsConfig.password().toCharArray());
        }
    }

    private static void initDefaultProgressMonitorStorage() {
        final var stdOutWriter = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        final var progressMonitor = new TextProgressMonitor(stdOutWriter);
        ProgressMonitorStorage.setProgressMonitor(progressMonitor);
    }

    private static void initGitStorage(final String repoPath) throws IOException {
        final var git = Git.open(new File(repoPath));
        GitStorage.setGit(git);

        Runtime.getRuntime().addShutdownHook(new Thread(git::close));
    }
}
