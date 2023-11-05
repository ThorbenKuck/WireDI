package com.wiredi.resources.builtin;

import com.wiredi.resources.ResolverContext;
import com.wiredi.resources.ResourceProtocolResolver;
import com.wiredi.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FileSystemResourceProtocolResolver implements ResourceProtocolResolver {

	public static final FileSystemResourceProtocolResolver INSTANCE = new FileSystemResourceProtocolResolver();

	@Override
	public @NotNull Resource resolve(@NotNull final ResolverContext resolverContext) {
		return new FileSystemResource(resolverContext.path());
	}

	@Override
	public @NotNull List<String> types() {
		return List.of("file");
	}
}
