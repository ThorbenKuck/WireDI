package com.wiredi.resources;

import com.wiredi.resources.builtin.ClassPathResourceProtocolResolver;
import com.wiredi.resources.builtin.FileSystemResourceProtocolResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ResourceLoader {

	@NotNull
	private final Map<@NotNull String, @NotNull ResourceProtocolResolver> protocolResolvers = new HashMap<>();
	@NotNull
	private final ResourceProtocolResolver defaultResolver = ClassPathResourceProtocolResolver.INSTANCE;
	private static final char PROTOCOL_DELIMITER = ':';

	@NotNull
	public static ResourceLoader open(final ResourceProtocolResolver... resolvers) {
		if (resolvers.length == 0) {
			return new ResourceLoader(ClassPathResourceProtocolResolver.INSTANCE, FileSystemResourceProtocolResolver.INSTANCE);
		} else {
			return new ResourceLoader(resolvers);
		}
	}

	public ResourceLoader(@NotNull final ResourceProtocolResolver... resolvers) {
		this(Arrays.asList(resolvers));
	}

	public ResourceLoader(@NotNull final Collection<@NotNull ResourceProtocolResolver> resolvers) {
		resolvers.forEach(this::addProtocolResolver);
	}

	public void addProtocolResolvers(@NotNull final Collection<? extends ResourceProtocolResolver> protocolResolvers) {
		protocolResolvers.forEach(this::addProtocolResolver);
	}

	public void addProtocolResolver(@NotNull final ResourceProtocolResolver resourceProtocolResolver) {
		resourceProtocolResolver.types().forEach(type -> {
			if (protocolResolvers.containsKey(type)) {
				throw new IllegalArgumentException("The ProtocolResolver " + resourceProtocolResolver + " tried to register the type " + type + " but there already is a ProtocolResolver registered for it");
			}
		});
		resourceProtocolResolver.types().forEach(type -> protocolResolvers.put(type, resourceProtocolResolver));
	}

	@NotNull
	public Resource load(@NotNull final String path) {
		final ResolverContext resolverContext = determinePathWithProtocol(path);
		if (resolverContext.protocol() == null) {
			return defaultResolver.resolve(resolverContext);
		}

		return Optional.ofNullable(protocolResolvers.get(resolverContext.protocol()))
				.map(resolver -> resolver.resolve(resolverContext))
				.orElseGet(() -> defaultResolver.resolve(resolverContext));
	}

	@NotNull
	public ResolverContext determinePathWithProtocol(@NotNull final String path) {
		final int index = path.indexOf(PROTOCOL_DELIMITER);
		if (index == -1) {
			return new ResolverContext(path, null);
		} else {
			final String protocol = path.substring(0, index);
			final String result = path.substring(index + 1);
			return new ResolverContext(result, protocol);
		}
	}
}
