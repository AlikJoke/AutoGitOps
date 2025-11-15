package ru.joke.git.commands;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.git.shared.JsonService;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AutoGitCommandFactory {

    private final Map<String, Class<AutoGitCommand<?, ?, ?>>> registry;
    private final JsonService jsonService;

    public AutoGitCommandFactory(final JsonService jsonService) {
        this.jsonService = jsonService;

        final Map<String, Class<AutoGitCommand<?, ?, ?>>> commandsMap = new HashMap<>();
        final var commandsRefs =
                ClassPathScanner.builder()
                                    .begin()
                                        .implementsInterface(AutoGitCommand.class)
                                    .build()
                                    .scan();
        commandsRefs
                .stream()
                .filter(command -> !command.aliases().isEmpty())
                .forEach(
                        command -> command.aliases().forEach(commandAlias -> commandsMap.put(commandAlias, loadClass(command)))
                );

        this.registry = Collections.unmodifiableMap(commandsMap);
    }

    public <R, C extends AutoGitCommand<R, C, B>, B extends AutoGitCommand.Builder<B, R, C>> AutoGitCommand<R, C, B> create(final String commandAlias, final String paramsJson) {
        final var commandClass = this.registry.get(commandAlias);
        @SuppressWarnings("unchecked")
        final var result = (AutoGitCommand<R, C, B>)
                (paramsJson == null
                        ? createUnconfiguredCommand(commandClass)
                        : this.jsonService.deserialize(paramsJson, commandClass));

        return result;
    }

    private AutoGitCommand<?, ?, ?> createUnconfiguredCommand(final Class<AutoGitCommand<?, ?, ?>> commandType) {
        try {
            final var defaultConstructor = commandType.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<AutoGitCommand<?, ?, ?>> loadClass(final ClassPathResource resource) {
        try {
            @SuppressWarnings("unchecked")
            final var classResource = (ClassResource<AutoGitCommand<?, ?, ?>>) resource;
            return classResource.asClass();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
