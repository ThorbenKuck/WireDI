package com.wiredi.environment;

import com.wiredi.domain.OrderComparator;
import com.wiredi.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.environment.resolvers.EnvironmentPropertyExpressionResolver;
import com.wiredi.lang.values.SafeReference;
import com.wiredi.properties.PropertyLoader;
import com.wiredi.properties.PropertyReference;
import com.wiredi.properties.TypeMapper;
import com.wiredi.properties.TypedProperties;
import com.wiredi.properties.keys.Key;
import com.wiredi.resources.Resource;
import com.wiredi.resources.ResourceLoader;
import com.wiredi.runtime.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.wiredi.environment.DefaultEnvironmentKeys.ACTIVE_PROFILES;

public class Environment {

    public static final Logger logger = LoggerFactory.getLogger(Environment.class);
    private static final String PLACE_HOLDER_START = "{";
    private static final String PLACE_HOLDER_END = "}";
    private static final PlaceholderResolver placeholderResolver = new PlaceholderResolver(PLACE_HOLDER_START, PLACE_HOLDER_END);
    private final Map<Character, EnvironmentExpressionResolver> expressionResolvers;
    private final TypedProperties properties = new TypedProperties();
    private final ResourceLoader resourceLoader = new ResourceLoader();
    private final PropertyLoader propertyLoader = new PropertyLoader();

    public Environment(EnvironmentExpressionResolver... expressionResolvers) {
        this(Arrays.asList(expressionResolvers));
    }

    public Environment(List<EnvironmentExpressionResolver> expressionResolvers) {
        this.expressionResolvers = expressionResolvers.stream().collect(Collectors.toMap(EnvironmentExpressionResolver::expressionIdentifier, Function.identity()));
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

    public void autoconfigure() {
        addExpressionResolvers(Loader.environmentExpressionResolvers());
        resourceLoader.addProtocolResolvers(Loader.protocolResolvers());
        propertyLoader.addPropertyFileLoaders(Loader.propertyFileLoaders());
        Loader.environmentConfigurations()
                .stream()
                .sorted(OrderComparator.INSTANCE)
                .forEach(config -> config.configure(this));
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

    public PropertyReference getPropertyReference(Key key) {
        return properties.getReferenceFor(key);
    }

    @NotNull
    public List<String> getAllProperties(Key key) {
        return new ArrayList<>(properties.getAll(key));
    }

    /**
     * Returns the raw Value of the key.
     *
     * @return an unresolved value of what the environment holds
     */
    @Nullable
    public String getProperty(@NotNull Key key, @Nullable String defaultValue) {
        return properties.get(key, defaultValue);
    }

    /**
     * Resolves all properties inside of this key
     *
     * @param key
     * @return
     */
    @NotNull
    public String resolve(String key) {
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(key);
        SafeReference<String> target = new SafeReference<>(key);
        placeholders.forEach(placeholder -> {
            final EnvironmentExpressionResolver resolver = getExpressionResolverFor(placeholder);

            Optional<String> replacement = resolver.resolve(placeholder, this);
            if (replacement.isPresent()) {
                target.update(value -> placeholder.replaceIn(value, replacement.get()));
            } else {
                target.updateIfPresent(placeholder::tryReplacementValueWithDefault);
            }
        });

        return target.getOrThrow(() -> new IllegalStateException("Could not resolve the key " + key));
    }

    private EnvironmentExpressionResolver getExpressionResolverFor(Placeholder placeholder) {
        if (placeholder.getIdentifierChar().isPresent()) {
            return placeholder.getIdentifierChar()
                    .map(expressionResolvers::get)
                    .orElseThrow(() -> new IllegalStateException("Could not find a suitable expression resolver for the expression identifier " + placeholder.getIdentifierChar().orElse(null)));
        } else {
            return new EnvironmentPropertyExpressionResolver();
        }
    }

    @NotNull
    public List<String> resolveList(String key) {
        return Arrays.asList(resolve(key).split(","));
    }

    @NotNull
    public <T> List<T> resolveList(
            @NotNull String key, Class<T> type) {
        return resolveList(key).stream()
                .map(it -> TypeMapper.convert(type, Key.just(key), it))
                .toList();
    }

    public <T> T resolveTyped(String key, Class<T> type) {
        return TypeMapper.convert(type, Key.just(key), resolve(key));
    }

    public <T> Optional<T> map(Key key, Function<String, T> function) {
        return Optional.ofNullable(getProperty(key))
                .map(function);
    }

    public List<String> activeProfiles() {
        return properties().getAll(ACTIVE_PROFILES);
    }

    public void printProfiles() {
        List<String> activeProfiles = activeProfiles();
        logger.atInfo().log(() -> {
            if (activeProfiles.isEmpty()) {
                return "No active profiles have been set";
            } else if (activeProfiles.size() == 1) {
                return "The following profile is active: " + activeProfiles.get(0);
            } else {
                return "The following " + activeProfiles.size() + " profiles are active: " + String.join(", ", activeProfiles);
            }
        });
    }
}
