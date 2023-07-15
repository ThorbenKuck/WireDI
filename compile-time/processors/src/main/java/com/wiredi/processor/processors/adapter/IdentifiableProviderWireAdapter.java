package com.wiredi.processor.processors.adapter;

import com.wiredi.annotations.Wire;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.processor.factories.AspectAwareProxyFactory;
import com.wiredi.processor.factories.IdentifiableProviderFactory;
import jakarta.inject.Inject;

import javax.lang.model.element.TypeElement;

public class IdentifiableProviderWireAdapter {

	private static final Logger logger = Logger.get(IdentifiableProviderWireAdapter.class);

	@Inject
	private AspectAwareProxyFactory aspectAwareProxyFactory;

	@Inject
	private IdentifiableProviderFactory identifiableProviderFactory;

	public void handle(
			TypeElement typeElement,
			Wire wire
	) {
		if (wire.proxy() && aspectAwareProxyFactory.create(typeElement) != null) {
			logger.debug(typeElement, () -> "Successfully created an aspect aware proxy");
			return;
		}
		if (identifiableProviderFactory.create(typeElement, wire) != null) {
			logger.debug(typeElement, () -> "Successfully created an identifiable provider");
			return;
		}

		logger.warn(typeElement, "INTERNAL ERROR: Failed to handle " + typeElement.getSimpleName() + " successfully: No artifact produced!");
	}
}
