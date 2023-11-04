package com.wiredi.tests;

import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.PreconditionViolationException;

public class WireDIExtension implements TestInstanceFactory, ParameterResolver {

	@Override
	public Object createTestInstance(
			TestInstanceFactoryContext factoryContext,
			ExtensionContext extensionContext
	) throws TestInstantiationException {
		return ExtensionCache.require(extensionContext)
				.tryGet(factoryContext.getTestClass())
				.orElseThrow(() -> new PreconditionViolationException("Could not create the class " + factoryContext.getTestClass()));
	}

	@Override
	public boolean supportsParameter(
			ParameterContext parameterContext,
			ExtensionContext extensionContext
	) throws ParameterResolutionException {
		TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
		WireRepository repository = ExtensionCache.require(extensionContext);
		return repository.supports(typeIdentifier);
	}

	@Override
	public Object resolveParameter(
			ParameterContext parameterContext,
			ExtensionContext extensionContext
	) throws ParameterResolutionException {
		TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
		WireRepository repository = ExtensionCache.require(extensionContext);

		if (typeIdentifier.isNativeProvider()) {
			return repository.getNativeProvider(typeIdentifier.getGenericTypes().get(0));
		} else if(typeIdentifier.isBean()) {
			return repository.getBean(typeIdentifier.getGenericTypes().get(0));
		} else {
			return repository.get(typeIdentifier);
		}
	}
}
