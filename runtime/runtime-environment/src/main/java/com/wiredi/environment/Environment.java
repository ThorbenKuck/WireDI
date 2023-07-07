package com.wiredi.environment;

import com.wiredi.domain.OrderComparator;
import com.wiredi.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.lang.SafeReference;
import com.wiredi.properties.PropertyLoader;
import com.wiredi.properties.PropertyReference;
import com.wiredi.properties.TypedProperties;
import com.wiredi.properties.keys.Key;
import com.wiredi.resources.Resource;
import com.wiredi.resources.ResourceLoader;
import com.wiredi.runtime.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Environment {

	private static final String PLACE_HOLDER_START = "{";
	private static final String PLACE_HOLDER_END = "}";
	private static final PlaceholderResolver placeholderResolver = new PlaceholderResolver(PLACE_HOLDER_START, PLACE_HOLDER_END);
	private final Map<Character, EnvironmentExpressionResolver> expressionResolvers;
	private final TypedProperties properties = new TypedProperties();
	private final ResourceLoader resourceLoader = new ResourceLoader();
	private final PropertyLoader propertyLoader = new PropertyLoader();

	public static Environment build() {
		Environment environment = new Environment(Loader.environmentExpressionResolvers());
		environment.resourceLoader.addProtocolResolvers(Loader.protocolResolvers());
		environment.propertyLoader.addPropertyFileLoaders(Loader.propertyFileLoaders());
		Loader.environmentConfigurations().forEach(config -> config.configure(environment));
		return environment;
	}

	public Environment(EnvironmentExpressionResolver... expressionResolvers) {
		this(Arrays.asList(expressionResolvers));
	}

	public Environment(List<EnvironmentExpressionResolver> expressionResolvers) {
		this.expressionResolvers = expressionResolvers.stream().collect(Collectors.toMap(EnvironmentExpressionResolver::identifier, Function.identity()));
	}

	public Environment() {
		this(Collections.emptyList());
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


	public void addExpressionResolvers(Collection<? extends EnvironmentExpressionResolver> resolver) {
		resolver.forEach(this::addExpressionResolver);
	}

	public void addExpressionResolver(EnvironmentExpressionResolver resolver) {
		if (expressionResolvers.containsKey(resolver.identifier())) {
			throw new IllegalArgumentException("An EnvironmentResolver for the type " + resolver.identifier() + " is already registered");
		}
		expressionResolvers.put(resolver.identifier(), resolver);
	}

	public Resource loadResource(String path) {
		return resourceLoader.load(resolve(path));
	}

	public void appendPropertiesFrom(Resource resource) {
		try (TypedProperties source = this.propertyLoader.load(resource)) {
			properties.setAll(source);
		}
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
			final EnvironmentExpressionResolver resolver = expressionResolvers.get(placeholder.getIdentifierChar());
			if (resolver != null) {
				Optional<String> replacement = resolver.resolve(Key.just(placeholder.getPlaceholderValue()), this);
				if (replacement.isPresent()) {
					target.update(value -> placeholder.replaceIn(value, replacement.get()));
				} else {
					target.updateIfPresent(placeholder::tryReplacementWithDefault);
				}
			}
		});

		return target.get();
	}

	public <T> Optional<T> map(Key key, Function<String, T> function) {
		return Optional.ofNullable(getProperty(key))
				.map(function);
	}
}
