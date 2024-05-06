package com.wiredi.runtime.resources.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.resources.ResolverContext;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import com.wiredi.runtime.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@AutoService(ResourceProtocolResolver.class)
public class FileSystemResourceProtocolResolver implements ResourceProtocolResolver {

	public static final FileSystemResourceProtocolResolver INSTANCE = new FileSystemResourceProtocolResolver();

	@Override
	public @NotNull Resource resolve(@NotNull final ResolverContext resolverContext) {
		return new FileSystemResource(resolverContext.path());
	}

	@Override
	public @NotNull Set<String> supportedProtocols() {
		return Set.of("file");
	}
}
