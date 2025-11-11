package ru.joke.git.commands;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.scanner.ClassPathScanner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AutoGitCommandFactory {

    private static final Map<String, Class<AutoGitCommand<?>>> registry;
    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DOTS)
            .create();

    static {
        final Map<String, Class<AutoGitCommand<?>>> commandsMap = new HashMap<>();
        final var commandsRefs = ClassPathScanner.builder().begin().implementsInterface(AutoGitCommand.class).build().scan();
        commandsRefs.stream().filter(command -> !command.aliases().isEmpty()).forEach(command -> command.aliases().forEach(commandAlias -> commandsMap.put(commandAlias, loadClass(command))));

        registry = Collections.unmodifiableMap(commandsMap);
    }

    public <T> AutoGitCommand<T> create(final String commandAlias, final String paramsJson) {
        final var commandClass = registry.get(commandAlias);
        @SuppressWarnings("unchecked")
        final var result = (AutoGitCommand<T>) gson.fromJson(paramsJson, commandClass);

        return result;
    }

    private static Class<AutoGitCommand<?>> loadClass(final ClassPathResource resource) {
        try {
            @SuppressWarnings("unchecked")
            final var classResource = (ClassResource<AutoGitCommand<?>>) resource;
            return classResource.asClass();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
