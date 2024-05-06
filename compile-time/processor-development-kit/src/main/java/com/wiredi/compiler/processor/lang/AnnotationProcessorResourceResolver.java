package com.wiredi.compiler.processor.lang;

import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.resources.ResolverContext;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import java.util.Set;

public class AnnotationProcessorResourceResolver implements ResourceProtocolResolver {

    private static final Logger logger = Logger.get(AnnotationProcessorResourceResolver.class);
    private final Filer filer;

    public AnnotationProcessorResourceResolver(Filer filer) {
        this.filer = filer;
    }

    @Override
    public @NotNull Resource resolve(@NotNull ResolverContext resolverContext) {
        logger.debug(() -> "Loading " + resolverContext.path() + " from filer.");
        return new AnnotationProcessorResource(filer, resolverContext.path());
    }

    @Override
    public @NotNull Set<String> supportedProtocols() {
        return Set.of("filer", "compiler");
    }
}
