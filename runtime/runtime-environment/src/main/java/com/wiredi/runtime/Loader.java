package com.wiredi.runtime;

import com.wiredi.domain.WireRepositoryContextCallbacks;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.lang.eager.EagerList;
import com.wiredi.lang.time.Timed;
import com.wiredi.environment.EnvironmentConfiguration;
import com.wiredi.properties.loader.PropertyFileLoader;
import com.wiredi.resources.ResourceProtocolResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import static com.wiredi.domain.Ordered.ordered;

public class Loader {

	private static final Logger logger = LoggerFactory.getLogger(Loader.class);
	private static final EagerList<IdentifiableProvider> providers = new EagerList<>(() -> load(IdentifiableProvider.class));
	private static final EagerList<WireRepositoryContextCallbacks> contextCallbacks = new EagerList<>(() -> ordered(load(WireRepositoryContextCallbacks.class)));
	private static final EagerList<EnvironmentExpressionResolver> environmentResolvers = new EagerList<>(() -> load(EnvironmentExpressionResolver.class));
	private static final EagerList<ResourceProtocolResolver> protocolResolvers = new EagerList<>(() -> load(ResourceProtocolResolver.class));
	private static final EagerList<PropertyFileLoader> propertyFileLoader = new EagerList<>(() -> load(PropertyFileLoader.class));
	private static final EagerList<EnvironmentConfiguration> environmentConfigurations = new EagerList<>(() -> ordered(load(EnvironmentConfiguration.class)));

	public static List<IdentifiableProvider> identifiableProviders() {
		return providers.content();
	}

	public static List<WireRepositoryContextCallbacks> contextCallbacks() {
		return contextCallbacks.content();
	}

	public static List<EnvironmentExpressionResolver> environmentExpressionResolvers() {
		return environmentResolvers.content();
	}

	public static List<ResourceProtocolResolver> protocolResolvers() {
		return protocolResolvers.content();
	}

	public static List<EnvironmentConfiguration> environmentConfigurations() {
		return environmentConfigurations.content();
	}

	public static List<PropertyFileLoader> propertyFileLoaders() {
		return propertyFileLoader.content();
	}

	public static <T> List<T> load(Class<T> type) {
		final List<T> content = new ArrayList<>();
		logger.trace("Starting to load " + type.getSimpleName());
		Timed timed = Timed.of(() -> ServiceLoader.load(type).forEach(content::add));
		logger.trace("Loaded " + type.getSimpleName() + " in {}ms", timed.get(TimeUnit.MILLISECONDS));
		return content;
	}
}
