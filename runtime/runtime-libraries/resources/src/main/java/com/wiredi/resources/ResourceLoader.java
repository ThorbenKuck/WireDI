package com.wiredi.resources;

import com.wiredi.resources.builtin.ClassPathResourceProtocolResolver;
import com.wiredi.resources.builtin.FileSystemResourceProtocolResolver;

import java.util.*;

public class ResourceLoader {

	private final Map<String, ResourceProtocolResolver> protocolResolvers = new HashMap<>();
	private final ResourceProtocolResolver defaultResolver = new ClassPathResourceProtocolResolver();
	private static final char PROTOCOL_DELIMITER = ':';

	public static ResourceLoader open(ResourceProtocolResolver... resolvers) {
		if (resolvers.length == 0) {
			return new ResourceLoader(new ClassPathResourceProtocolResolver(), new FileSystemResourceProtocolResolver());
		} else {
			return new ResourceLoader(resolvers);
		}
	}

	public ResourceLoader(ResourceProtocolResolver... resolvers) {
		this(Arrays.asList(resolvers));
	}

	public ResourceLoader(Collection<ResourceProtocolResolver> resolvers) {
		resolvers.forEach(this::addProtocolResolver);
	}

	public void addProtocolResolvers(Collection<? extends ResourceProtocolResolver> protocolResolvers) {
		protocolResolvers.forEach(this::addProtocolResolver);
	}

	public void addProtocolResolver(ResourceProtocolResolver resourceProtocolResolver) {
		resourceProtocolResolver.types().forEach(type -> {
			if (protocolResolvers.containsKey(type)) {
				throw new IllegalArgumentException("The ProtocolResolver " + resourceProtocolResolver + " tried to register the type " + type + " but there already is a ProtocolResolver registered for it");
			}
		});
		resourceProtocolResolver.types().forEach(type -> protocolResolvers.put(type, resourceProtocolResolver));
	}

	public Resource load(String path) {
		PathWithProtocol pathWithProtocol = determinePathWithProtocol(path);
		if (pathWithProtocol.protocol() == null) {
			return getDefault(pathWithProtocol.path());
		}

		return Optional.ofNullable(protocolResolvers.get(pathWithProtocol.protocol()))
				.map(resolver -> resolver.resolve(pathWithProtocol.path()))
				.orElseGet(() -> getDefault(pathWithProtocol.path()));
	}

	public PathWithProtocol determinePathWithProtocol(String path) {
		int index = path.indexOf(PROTOCOL_DELIMITER);
		if (index == -1) {
			return new PathWithProtocol(path, null);
		} else {
			String protocol = path.substring(0, index);
			String result = path.substring(index + 1);
			return new PathWithProtocol(result, protocol);
		}
	}

	private Resource getDefault(String path) {
		return defaultResolver.resolve(path);
	}
}
