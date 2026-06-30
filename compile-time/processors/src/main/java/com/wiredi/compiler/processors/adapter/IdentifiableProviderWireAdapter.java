package com.wiredi.compiler.processors.adapter;

import com.wiredi.annotations.ProxyMode;
import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.entities.AspectAwareProxyEntity;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.compiler.processor.factories.AspectAwareProxyFactory;
import com.wiredi.compiler.processor.factories.IdentifiableProviderFactory;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.domain.annotations.AnnotationExcerpt;
import com.wiredi.runtime.properties.Key;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

public class IdentifiableProviderWireAdapter {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(IdentifiableProviderWireAdapter.class);
    private final Environment environment;
    private final AspectAwareProxyFactory aspectAwareProxyFactory;
    private final IdentifiableProviderFactory identifiableProviderFactory;

    public IdentifiableProviderWireAdapter(Environment environment, AspectAwareProxyFactory aspectAwareProxyFactory, IdentifiableProviderFactory identifiableProviderFactory) {
        this.environment = environment;
        this.aspectAwareProxyFactory = aspectAwareProxyFactory;
        this.identifiableProviderFactory = identifiableProviderFactory;
    }

    @Nullable
    public AspectAwareProxyEntity tryCreateProxy(
            TypeElement typeElement,
            @Nullable AnnotationExcerpt<Wire> wireAnnotationExcerpt
    ) {
        ProxyMode proxyBeans = environment.getProperty(Key.just("wiredi.proxy-mode"), ProxyMode.class, ProxyMode.OPT_IN);
        Boolean proxyAnnotationField = Optional.ofNullable(wireAnnotationExcerpt).map(AnnotationExcerpt::metadata)
                .flatMap(it -> it.getBoolean("proxy"))
                .orElse(null);
        boolean proxyClass = proxyBeans.shouldProxy(proxyAnnotationField);

        if (proxyClass) {
            AspectAwareProxyEntity proxyEntity = aspectAwareProxyFactory.create(typeElement);
            logger.debug(typeElement, () -> "Successfully created an aspect aware proxy");
            return proxyEntity;
        }

        return null;
    }

    @Nullable
    public IdentifiableProviderEntity tryCreateIdentifiableProvider(
            TypeElement typeElement,
            @Nullable AnnotationExcerpt<Wire> wireAnnotationExcerpt
    ) {
        IdentifiableProviderEntity entity = identifiableProviderFactory.create(typeElement, Optional.ofNullable(wireAnnotationExcerpt).map(AnnotationExcerpt::instance).orElse(null));
        if (entity != null) {
            logger.debug(typeElement, () -> "Successfully created an identifiable provider");
            return entity;
        }

        return null;
    }

    public void handle(
            @NotNull TypeElement typeElement,
            @Nullable AnnotationExcerpt<Wire> wireAnnotationExcerpt
    ) {
        AspectAwareProxyEntity proxy = tryCreateProxy(typeElement, wireAnnotationExcerpt);
        if (proxy != null) {
            return;
        }

        IdentifiableProviderEntity identifiableProvider = tryCreateIdentifiableProvider(typeElement, wireAnnotationExcerpt);
        if (identifiableProvider != null) {
            return;
        }

        logger.warn(typeElement, "INTERNAL ERROR: Failed to handle " + typeElement.getSimpleName() + " successfully: No artifact produced!");
    }
}
