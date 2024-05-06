package com.wiredi.runtime.resources.builtin;

import com.wiredi.runtime.resources.ResolverContext;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import com.wiredi.runtime.resources.exceptions.UnsupportedResourceProtocolException;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class UnsupportedResourceProtocolResolver implements ResourceProtocolResolver {

    public static UnsupportedResourceProtocolResolver INSTANCE = new UnsupportedResourceProtocolResolver();

    @Override
    public @NotNull Resource resolve(@NotNull ResolverContext resolverContext) {
        throw new UnsupportedResourceProtocolException(resolverContext.protocol());
    }

    @Override
    public @NotNull Set<String> supportedProtocols() {
        return Collections.emptySet();
    }
}
