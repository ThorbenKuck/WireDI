package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.environment.DefaultEnvironmentKeys;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.environment.Placeholder;
import com.wiredi.runtime.environment.PlaceholderResolver;
import com.wiredi.runtime.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.runtime.lang.OrderedComparator;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.properties.PropertyLoader;
import com.wiredi.runtime.properties.TypedProperties;
import com.wiredi.runtime.properties.loader.PropertyFileTypeLoader;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.ResourceLoader;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.types.TypeConverter;
import com.wiredi.runtime.types.TypeMapper;
import com.wiredi.runtime.types.exceptions.InvalidPropertyTypeException;
import com.wiredi.runtime.values.SafeReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An Environment is maintaining a state of meta-data for the WireRepository and the runtime context.
 * <p>
 * Its main purpose is to maintain file-access and properties and is deeply connected into the WireRepository.
 * To access the Environment, call {@link WireContainer#environment()}.
 * <p>
 * You can modify the Environment by providing {@link EnvironmentConfiguration}.
 * For details see the {@link EnvironmentConfiguration} configuration.
 * <p>
 * Notes: In general, it is not recommended to manually construct an Environment, but if you really have to,
 * make sure to call {@link #autoconfigure()}.
 * Otherwise, try to confine the usages to instances maintained in the {@link WireContainer}.
 * <p>
 * The environment can be used generically, by using {@link #resolve(String)}
 *
 * @see WireContainer
 * @see EnvironmentExpressionResolver
 * @see EnvironmentConfiguration
 */
public class Environment {

    private static final Logging logger = Logging.getInstance(Environment.class);
    private static final String PLACE_HOLDER_START = "{";
    private static final String PLACE_HOLDER_END = "}";
    private static final PlaceholderResolver placeholderResolver = new PlaceholderResolver(PLACE_HOLDER_START, PLACE_HOLDER_END);
    private final SortedSet<EnvironmentConfiguration> configurations = new TreeSet<>(OrderedComparator.INSTANCE);
    private final Map<Character, EnvironmentExpressionResolver> expressionResolvers;
    private final TypeMapper typeMapper = TypeMapper.newPreconfigured();
    private final TypedProperties properties = new TypedProperties(typeMapper);
    private final ResourceLoader resourceLoader = new ResourceLoader();
    private final PropertyLoader propertyLoader = new PropertyLoader();
    private final ServiceFiles<EnvironmentExpressionResolver> environmentExpressionResolverServiceFiles = ServiceFiles.getInstance(EnvironmentExpressionResolver.class);
    private final ServiceFiles<ResourceProtocolResolver> resourceProtocolResolverServiceFiles = ServiceFiles.getInstance(ResourceProtocolResolver.class);
    private final ServiceFiles<PropertyFileTypeLoader> propertyFileTypeLoaderServiceFiles = ServiceFiles.getInstance(PropertyFileTypeLoader.class);
    private final ServiceFiles<EnvironmentConfiguration> environmentConfigurationServiceFiles = ServiceFiles.getInstance(EnvironmentConfiguration.class);
    private boolean loaded = false;

    public Environment(EnvironmentExpressionResolver... expressionResolvers) {
        this(Arrays.asList(expressionResolvers));
    }

    public Environment(List<EnvironmentExpressionResolver> expressionResolvers) {
        this.expressionResolvers = expressionResolvers.stream().collect(Collectors.toMap(EnvironmentExpressionResolver::expressionIdentifier, Function.identity()));
    }

    public Environment() {
        this(Collections.emptyList());
    }

    /**
     * Constructs a new, autonomous and autoconfigured {@link Environment}.
     *
     * @return a new instance of the environment.
     */
    public static Environment build() {
        Environment environment = new Environment();
        environment.autoconfigure();
        return environment;
    }

    /**
     * The properties maintained in this Environment.
     * <p>
     * You can use this method if you need to access the properties in a way that is not provided by the environment itself.
     *
     * @return the maintained {@link TypedProperties} instance
     * @see TypedProperties
     */
    public TypedProperties properties() {
        return properties;
    }

    /**
     * The {@link ResourceLoader} that is configured by and maintained in this Environment.
     * <p>
     * A {@link ResourceLoader} can be used to dynamically load resources,
     * by providing a string that follows this schema {@code <protocol>:<source>}.
     * For example, if you want to access a class path resource: {@code classpath:my-resource.txt}.
     * For details consult the documentation of the {@link ResourceLoader}.
     *
     * @see ResourceLoader
     */
    public ResourceLoader resourceLoader() {
        return resourceLoader;
    }

    /**
     * Returns the {@link PropertyLoader} configured and maintained by the Environment instance.
     * <p>
     * A {@link PropertyLoader} can be used to load a {@link Resource} into a new {@link TypedProperties} instance.
     * It knows how to interpret different file-types based on {@link PropertyFileTypeLoader} instances.
     * <p>
     * For further details see the documentation of the {@link PropertyLoader}
     *
     * @return the maintained property loader
     * @see PropertyLoader
     * @see TypedProperties
     * @see PropertyFileTypeLoader
     */
    public PropertyLoader propertyLoader() {
        return propertyLoader;
    }

    /**
     * Returns the {@link TypeMapper} configured and maintained by the Environment instance.
     * <p>
     * A {@link TypeMapper} is responsible for converting simple types.
     * For example, it defines how to convert a {@link String} to a {@link Boolean} or any other primitives.
     * <p>
     * Though it is possible to define more complex, non-primitive mappings in the {@link TypeMapper}, for more complex
     * mappings it is recommended to use the messaging module instead.
     * <p>
     * For further details see the documentation of the {@link TypeMapper}
     *
     * @return the maintained {@link TypeMapper}
     * @see TypeMapper
     * @see TypeConverter
     */
    public TypeMapper typeMapper() {
        return typeMapper;
    }

    /**
     * Whether the environment is set to "debug".
     * <p>
     * Debug mode is controlled simply by setting the property "debug" to "true".
     * Alternatively to using this method, any developer can just request the debug property.
     * This method serves as a utility method for ease of use
     *
     * @return true, if debug mode is enabled
     */
    public boolean debugEnabled() {
        return getProperty(PropertyKeys.DEBUG.getKey(), false);
    }

    /**
     * Clears the state of this environment.
     * <p>
     * Subsequent interactions with the environment would yield no results.
     */
    public void clear() {
        expressionResolvers.clear();
        resourceLoader.clear();
        propertyLoader.clear();
        properties.clear();
        typeMapper.takeTypeConvertersFrom(TypeMapper.getInstance());
        this.loaded = false;
    }

    public Environment addConfiguration(EnvironmentConfiguration configuration) {
        this.configurations.add(configuration);
        return this;
    }

    /**
     * This method configures this Environment instance based on the {@link java.util.ServiceLoader}.
     * <p>
     * It will attempt to load:
     * <ul>
     *     <li>{@link EnvironmentExpressionResolver}</li>
     *     <li>{@link com.wiredi.runtime.resources.ResourceProtocolResolver}</li>
     *     <li>{@link PropertyFileTypeLoader}</li>
     *     <li>{@link EnvironmentConfiguration}</li>
     * </ul>
     *
     * @return How long the autoconfiguration took.
     */
    public Timed autoconfigure() {
        if (loaded) {
            return Timed.ZERO;
        }
        return Timed.of(() -> {
            addExpressionResolvers(environmentExpressionResolverServiceFiles.instances());
            resourceLoader.addProtocolResolvers(resourceProtocolResolverServiceFiles.instances());
            propertyLoader.addPropertyFileLoaders(propertyFileTypeLoaderServiceFiles.instances());
            configurations.addAll(environmentConfigurationServiceFiles.instances());
            configurations.forEach(config -> config.configure(this));
            this.loaded = true;
        }).then(time -> logger.debug("Environment autoconfigured in " + time));
    }

    /**
     * Loads the properties from the provided path.
     * <p>
     * This is done by converting the path into a {@link Resource} through the {@link ResourceLoader}
     * and then delegating this to the {@link PropertyLoader}
     *
     * @param path the path to load
     * @return A new instance of {@link TypedProperties} containing the properties found in the resource.
     * @see ResourceLoader
     * @see PropertyLoader
     */
    public TypedProperties loadProperties(String path) {
        Resource resource = resourceLoader.load(path);
        return propertyLoader.load(resource);
    }

    /**
     * Loads a new TypedProperties instance based on the resource.
     *
     * @param resource the resource to load
     * @return a new TypedProperties instance containing the properties located in the {@link Resource}
     */
    public TypedProperties loadProperties(Resource resource) {
        return propertyLoader.load(resource);
    }

    /**
     * Converts the {@code input} into the requested {@code type}
     *
     * @param input The object to convert
     * @param type  The type to convert the input into
     * @param <T>   a generic of the target type
     * @return A new instance of the {@code type}
     * @throws InvalidPropertyTypeException when the property could not be converted
     */
    @NotNull
    public <T> T convert(
            @NotNull Object input,
            @NotNull Class<T> type
    ) throws InvalidPropertyTypeException {
        return typeMapper.convert(input, type);
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

    // ### Properties operations ###

    public void appendPropertiesFrom(Resource resource) {
        logger.debug(() -> "Appending properties from " + resource.getPath());
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

    public void setProperties(Map<Key, String> properties) {
        this.properties.setAll(properties);
    }

    public void setProperties(Properties properties) {
        this.properties.setAll(properties);
    }

    public void setProperties(TypedProperties properties) {
        this.properties.setAll(properties);
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
    public <T, S extends T> T getPropertyOrResolve(@NotNull Key key, @NotNull Class<T> type, String defaultValue) {
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
        return properties.getAll(key, raw -> typeMapper.convert(raw, type));
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
        placeholders.forEach(placeholder -> resolvePlaceholder(placeholder, target));
        return target.getOrThrow(() -> new IllegalStateException("Could not resolve the key " + content));
    }

    private void resolvePlaceholder(Placeholder placeholder, SafeReference<String> target) {
        getExpressionResolverFor(placeholder)
                .flatMap(resolver -> resolver.resolve(placeholder, this))
                .ifPresent(resolved -> target.update(value -> placeholder.replaceIn(value, resolved)));
    }

    @NotNull
    private Optional<EnvironmentExpressionResolver> getExpressionResolverFor(Placeholder placeholder) {
        if (placeholder.getIdentifierChar().isPresent()) {
            return placeholder.getIdentifierChar()
                    .map(expressionResolvers::get);
        } else {
            return Optional.empty();
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
                .map(it -> typeMapper.convert(it, type))
                .toList();
    }

    public <T> T resolveTyped(String key, Class<T> type) {
        return typeMapper.convert(resolve(key), type);
    }

    public <T> Optional<T> map(Key key, Function<String, T> function) {
        return Optional.ofNullable(getProperty(key))
                .map(function);
    }

    public void addActiveProfile(String profile) {
        List<String> profiles = this.properties.getAll(DefaultEnvironmentKeys.ACTIVE_PROFILES);

        if (!profiles.contains(profile)) {
            this.properties.add(DefaultEnvironmentKeys.ACTIVE_PROFILES, profile);
        }
    }

    public List<String> activeProfiles() {
        return getAllProperties(DefaultEnvironmentKeys.ACTIVE_PROFILES);
    }

    public void printProfiles() {
        List<String> activeProfiles = activeProfiles();
        logger.info(() -> {
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
