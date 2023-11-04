package com.wiredi.processor.processors.adapter;

import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.entities.AspectAwareProxyEntity;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.processor.factories.AspectAwareProxyFactory;
import com.wiredi.processor.factories.IdentifiableProviderFactory;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

public class IdentifiableProviderWireAdapter {

	private static final Logger logger = Logger.get(IdentifiableProviderWireAdapter.class);

	@Inject
	private AspectAwareProxyFactory aspectAwareProxyFactory;

	@Inject
	private IdentifiableProviderFactory identifiableProviderFactory;

	@Nullable
	public AspectAwareProxyEntity tryCreateProxy(
			TypeElement typeElement,
			@Nullable Wire wire
	) {
		if (Optional.ofNullable(wire).map(Wire::proxy).orElse(true)) {
			AspectAwareProxyEntity proxyEntity = aspectAwareProxyFactory.create(typeElement);
			logger.debug(typeElement, () -> "Successfully created an aspect aware proxy");
			return proxyEntity;
		}

		return null;
	}

	@Nullable
	public IdentifiableProviderEntity tryCreateIdentifiableProvider(
			TypeElement typeElement,
			@Nullable Wire wire
	) {
		IdentifiableProviderEntity entity = identifiableProviderFactory.create(typeElement, wire);
		if (entity != null) {
			logger.debug(typeElement, () -> "Successfully created an identifiable provider");
			return entity;
		}

		return null;
	}

	public void handle(
			TypeElement typeElement,
			@Nullable Wire wire
	) {
		AspectAwareProxyEntity proxy = tryCreateProxy(typeElement, wire);
		if (proxy != null) {
			return;
		}

		IdentifiableProviderEntity identifiableProvider = tryCreateIdentifiableProvider(typeElement, wire);
		if (identifiableProvider != null) {
			return;
		}

		logger.warn(typeElement, "INTERNAL ERROR: Failed to handle " + typeElement.getSimpleName() + " successfully: No artifact produced!");
	}
}
