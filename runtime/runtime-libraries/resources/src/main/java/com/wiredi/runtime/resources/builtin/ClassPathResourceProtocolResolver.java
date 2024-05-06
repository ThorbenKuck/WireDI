package com.wiredi.runtime.resources.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.resources.ResolverContext;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@AutoService(ResourceProtocolResolver.class)
public class ClassPathResourceProtocolResolver implements ResourceProtocolResolver {

    public static final ClassPathResourceProtocolResolver INSTANCE = new ClassPathResourceProtocolResolver();

    @Override
    public @NotNull Resource resolve(@NotNull final ResolverContext resolverContext) {
        return new ClassPathResource(resolverContext.path());
    }

    @Override
    public @NotNull Set<String> supportedProtocols() {
        return Set.of("classpath");
    }
}
