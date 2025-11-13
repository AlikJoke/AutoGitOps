package ru.joke.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;
import ru.joke.git.commands.AutoGitCommandFactory;
import ru.joke.git.shared.GitStorage;
import ru.joke.git.shared.JsonService;
import ru.joke.git.shared.ProgressMonitorStorage;
import ru.joke.git.shared.WindowCacheConfigurer;
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
    private static final String EXIT_COMMAND = "exit";

    private static final String AWAIT_NEXT_COMMAND_INFO = "Ready to next commands...";
    private static final String STARTED_INFO = "Started, ready to execute commands";
    private static final String STOPPED_INFO = "Successfully stopped";

    private static final String PK_FILE_PATH_PARAM = "pk.file.path";
    private static final String USE_LEGACY_KEX_ALGORITHMS_PARAM = "use.legacy.kex";
    private static final String PASSPHRASE_PARAM = "passphrase";

    private static final String CREDENTIALS_USERNAME_PARAM = "credentials.username";
    private static final String CREDENTIALS_PASSWORD_PARAM = "credentials.password";

    private static final String REPOSITORY_PATH = "repo.path";

    private static final JsonService jsonService = new JsonService();
    private static final AutoGitCommandFactory commandFactory = new AutoGitCommandFactory(jsonService);

    static void main(String[] args) throws GeneralSecurityException, IOException {

        initializeSharedResources(args);

        println(STARTED_INFO);

        while (true) {
            final String command = readln();
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
            final var commandData = command.split("=", 2);
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
                        .map(arg -> arg.split("="))
                        .filter(arg -> arg.length > 1)
                        .collect(Collectors.toMap(arg -> arg[0], arg -> arg[1]));
    }

    private static void initializeSharedResources(final String[] args) throws GeneralSecurityException, IOException {

        final var argsMap = parseArgs(args);

        final var pkFilePath = argsMap.get(PK_FILE_PATH_PARAM);
        final var passphrase = argsMap.get(PASSPHRASE_PARAM);

        if (pkFilePath != null && !pkFilePath.isBlank()) {
            final var useLegacyKexAlgorithms = Boolean.parseBoolean(argsMap.get(USE_LEGACY_KEX_ALGORITHMS_PARAM));

            final var sshInitializer = new SshdSessionFactoryInitializer();
            sshInitializer.initialize(pkFilePath, passphrase, useLegacyKexAlgorithms);
        }

        final var username = argsMap.get(CREDENTIALS_USERNAME_PARAM);
        final var password = argsMap.get(CREDENTIALS_PASSWORD_PARAM);

        if (username != null && !username.isBlank()) {
            final var credentialsInitializer = new GlobalCredentialsInitializer();
            credentialsInitializer.initialize(username, password);
        }

        final var repoPath = argsMap.get(REPOSITORY_PATH);
        if (repoPath != null) {
            final var git = Git.open(new File(repoPath));
            GitStorage.setGit(git);

            Runtime.getRuntime().addShutdownHook(new Thread(git::close));
        }

        final var stdOutWriter = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        final var progressMonitor = new TextProgressMonitor(stdOutWriter);
        ProgressMonitorStorage.setProgressMonitor(progressMonitor);

        final var configurer = new WindowCacheConfigurer();
        configurer.configure(argsMap);
    }
}
