package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.OrderedComparator;
import com.wiredi.runtime.environment.DefaultEnvironmentKeys;
import com.wiredi.runtime.environment.Placeholder;
import com.wiredi.runtime.environment.PlaceholderResolver;
import com.wiredi.runtime.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.runtime.properties.*;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.ResourceLoader;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.values.SafeReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Environment {

    public static final Logging LOGGER = Logging.getInstance(Environment.class);
    private static final String PLACE_HOLDER_START = "{";
    private static final String PLACE_HOLDER_END = "}";
    private static final PlaceholderResolver placeholderResolver = new PlaceholderResolver(PLACE_HOLDER_START, PLACE_HOLDER_END);
    private static final Key TAKE_ENVIRONMENT_PROPERTIES = Key.just("environment.autoconfiguration.take-environment-properties");
    private static final Key TAKE_SYSTEM_PROPERTIES = Key.just("environment.autoconfiguration.take-system-properties");
    private final Map<Character, EnvironmentExpressionResolver> expressionResolvers;
    private final TypeMapper typeMapper = TypeMapper.newPreconfigured();
    private final TypedProperties properties = new TypedProperties(typeMapper);
    private final ResourceLoader resourceLoader = new ResourceLoader();
    private final PropertyLoader propertyLoader = new PropertyLoader();
    private final ServiceLoader loader;

    public Environment(ServiceLoader loader, EnvironmentExpressionResolver... expressionResolvers) {
        this(loader, Arrays.asList(expressionResolvers));
    }

    public Environment(EnvironmentExpressionResolver... expressionResolvers) {
        this(Arrays.asList(expressionResolvers));
    }

    public Environment(List<EnvironmentExpressionResolver> expressionResolvers) {
        this(ServiceLoader.getInstance(), expressionResolvers);
    }

    public Environment(ServiceLoader loader, List<EnvironmentExpressionResolver> expressionResolvers) {
        this.expressionResolvers = expressionResolvers.stream().collect(Collectors.toMap(EnvironmentExpressionResolver::expressionIdentifier, Function.identity()));
        this.loader = loader;
    }

    public Environment() {
        this(Collections.emptyList());
    }

    public static Environment build() {
        Environment environment = new Environment();
        environment.autoconfigure();
        return environment;
    }

    public TypedProperties properties() {
        return properties;
    }

    public ResourceLoader resourceLoader() {
        return resourceLoader;
    }

    public PropertyLoader propertyLoader() {
        return propertyLoader;
    }

    public TypeMapper typeMapper() {
        return typeMapper;
    }

    public void clear() {
        expressionResolvers.clear();
        resourceLoader.clear();
        propertyLoader.clear();
        properties.clear();
        typeMapper.setTypeConverters(TypeMapper.getInstance());
    }

    public Timed autoconfigure() {
        return Timed.of(() -> {
            addExpressionResolvers(loader.environmentExpressionResolvers());
            resourceLoader.addProtocolResolvers(loader.protocolResolvers());
            propertyLoader.addPropertyFileLoaders(loader.propertyFileLoaders());
            loader.environmentConfigurations()
                    .stream()
                    .sorted(OrderedComparator.INSTANCE)
                    .forEach(config -> config.configure(this));

            if (properties.getBoolean(TAKE_ENVIRONMENT_PROPERTIES, true)) {
                LOGGER.debug("Appending all JVM environment variables to the environment");
                System.getenv().forEach((key, value) -> {
                    properties.set(Key.format(key), resolve(value));
                });
            }

            if (properties.getBoolean(TAKE_SYSTEM_PROPERTIES, true)) {
                LOGGER.debug("Adding all system variables to the environment");
                System.getProperties()
                        .stringPropertyNames().forEach(property -> {
                            properties.set(Key.format(property), resolve(System.getProperty(property)));
                        });
            }
        }).then(time -> LOGGER.debug("Environment autoconfigured in " + time));
    }

    public TypedProperties loadProperties(String path) {
        Resource resource = loadResource(path);
        return propertyLoader.load(resource);
    }

    public TypedProperties loadProperties(Resource resource) {
        return propertyLoader.load(resource);
    }

    public void addExpressionResolvers(Collection<? extends EnvironmentExpressionResolver> resolver) {
        resolver.forEach(this::addExpressionResolver);
    }

    public void addExpressionResolver(EnvironmentExpressionResolver resolver) {
        if (expressionResolvers.containsKey(resolver.expressionIdentifier())) {
            throw new IllegalArgumentException("An EnvironmentResolver for the type " + resolver.expressionIdentifier() + " is already registered");
        }
        expressionResolvers.put(resolver.expressionIdentifier(), resolver);
    }

    public Resource loadResource(String path) {
        return resourceLoader.load(resolve(path));
    }

    public Resource loadResource(String path, String defaultProtocol) {
        return resourceLoader.load(resolve(path), defaultProtocol);
    }

    public void appendPropertiesFrom(Resource resource) {
        try (TypedProperties source = this.propertyLoader.load(resource)) {
            properties.setAll(source);
        }
    }

    public void appendPropertiesFrom(TypedProperties newProperties) {
        this.properties.setAll(newProperties);
    }

    public void setProperty(Key key, String value) {
        properties.set(key, resolve(value));
    }

    /**
     * Returns the raw Value of the key.
     *
     * @return an unresolved value of what the environment holds
     */
    @Nullable
    public String getProperty(Key key) {
        return properties.get(key).orElse(null);
    }

    /**
     * Returns the raw Value of the key.
     *
     * @return an unresolved value of what the environment holds
     */
    @NotNull
    public String getProperty(@NotNull Key key, @NotNull String defaultValue) {
        return properties.get(key, defaultValue);
    }

    @NotNull
    public <T> T getProperty(@NotNull Key key, @NotNull T defaultValue) {
        return properties.get(key, (Class<T>) defaultValue.getClass(), defaultValue);
    }

    @NotNull
    public <T> Optional<T> getProperty(@NotNull Key key, @NotNull Class<T> type) {
        return properties.tryGet(key, type);
    }

    @NotNull
    public <T, S extends T> T getProperty(@NotNull Key key, @NotNull Class<T> type, S defaultValue) {
        return properties.get(key, type, defaultValue);
    }

    @NotNull
    public <T, S extends T> T getProperty(@NotNull Key key, @NotNull Class<T> type, Supplier<S> defaultValue) {
        return properties.get(key, type, defaultValue);
    }

    @NotNull
    public List<String> getAllProperties(Key key) {
        return properties.getAll(key);
    }

    @NotNull
    public <T> List<T> getAllProperties(Key key, Class<T> type) {
        return properties.getAll(key, raw -> typeMapper.parse(type, raw));
    }

    /**
     * Determines all placeholders in the content and tries to resolve them.
     * <p>
     * Placeholders are defined like this:
     * - The first char is a char other than '{'. This is called the identifier char.
     * - The second char is exactly a '{'
     * - The last char is exactly a '}'
     * <p>
     * This can be anything from `${...}` to `X{...}`.
     * <p>
     * The value inside the brackets is than resolved by a {@link EnvironmentExpressionResolver} that is identified by
     * the identifier char.
     * If a PlaceholderResolver is determined for the identifier char,
     * the provided value will replace the placeholder from the identifier char up to and including the last '}'.
     * Otherwise, the whole placeholder is left untouched.
     * <p>
     * The placeholder must be part of the string.
     * The whole string can be the placeholder (for example, "${my.test}").
     * The placeholder can be located anywhere in the string (for example, "This is ${global.beautiful}, isn't it?").
     * <p>
     * Placeholders support parameters.
     * To add a parameter, enter a ":" as a separator inside the placeholder.
     * For example, `${my.property:default}`.
     * <p>
     * The {@link EnvironmentExpressionResolver} for the identifier char is then responsible to process the parameters.
     * It can choose to ignore them or to do further things with the parameters (like splitting them).
     * In the example of the default {@link com.wiredi.runtime.environment.resolvers.EnvironmentPropertyExpressionResolver}
     * which is bound to the identifier char '$',
     * the parameter will be used as a default value of the property is absent.
     *
     * @param content the content to search for placeholders
     * @return a new string. All placeholders for which a PlaceholderResolver could be identified, the placeholder will be replaced.
     * @see EnvironmentExpressionResolver
     * @see PlaceholderResolver
     */
    @NotNull
    public String resolve(String content) {
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(content);
        SafeReference<String> target = new SafeReference<>(content);
        placeholders.forEach(placeholder -> {
            final EnvironmentExpressionResolver resolver = getExpressionResolverFor(placeholder);

            if (resolver != null) {
                Optional<String> replacement = resolver.resolve(placeholder, this);
                if (replacement.isPresent()) {
                    target.update(value -> placeholder.replaceIn(value, replacement.get()));
                } else {
                    target.updateIfPresent(placeholder::tryReplacementValueWithDefault);
                }
            }
        });

        return target.getOrThrow(() -> new IllegalStateException("Could not resolve the key " + content));
    }

    @Nullable
    private EnvironmentExpressionResolver getExpressionResolverFor(Placeholder placeholder) {
        if (placeholder.getIdentifierChar().isPresent()) {
            return placeholder.getIdentifierChar()
                    .map(expressionResolvers::get)
                    .orElse(null);
        } else {
            return null;
        }
    }

    @NotNull
    public List<String> resolveList(String key) {
        return Arrays.asList(resolve(key).split(","));
    }

    @NotNull
    public <T> List<T> resolveList(
            @NotNull String key, Class<T> type
    ) {
        return resolveList(key).stream()
                .map(it -> typeMapper.parse(type, Key.just(key), it))
                .toList();
    }

    public <T> T resolveTyped(String key, Class<T> type) {
        return typeMapper.parse(type, Key.just(key), resolve(key));
    }

    public <T> Optional<T> map(Key key, Function<String, T> function) {
        return Optional.ofNullable(getProperty(key))
                .map(function);
    }

    public List<String> activeProfiles() {
        return getAllProperties(DefaultEnvironmentKeys.ACTIVE_PROFILES);
    }

    public void printProfiles() {
        List<String> activeProfiles = activeProfiles();
        LOGGER.info(() -> {
            if (activeProfiles.isEmpty()) {
                return "No active profiles have been set";
            } else if (activeProfiles.size() == 1) {
                return "The following profile is active: " + activeProfiles.getFirst();
            } else {
                return "The following " + activeProfiles.size() + " profiles are active: " + String.join(", ", activeProfiles);
            }
        });
    }
}
