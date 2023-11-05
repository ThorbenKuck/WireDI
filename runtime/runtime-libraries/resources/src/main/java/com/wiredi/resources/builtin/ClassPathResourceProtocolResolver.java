package com.wiredi.resources.builtin;

import com.wiredi.resources.ResolverContext;
import com.wiredi.resources.Resource;
import com.wiredi.resources.ResourceProtocolResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClassPathResourceProtocolResolver implements ResourceProtocolResolver {

    public static final ClassPathResourceProtocolResolver INSTANCE = new ClassPathResourceProtocolResolver();

    @Override
    public @NotNull Resource resolve(@NotNull final ResolverContext resolverContext) {
        return new ClassPathResource(resolverContext.path());
    }

    @Override
    public @NotNull List<String> types() {
        return List.of("classpath");
    }
}
