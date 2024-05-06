package com.wiredi.tests;

import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.extension.*;

public class WireDIExtension implements TestInstanceFactory, ParameterResolver {

	@Override
	public Object createTestInstance(
			TestInstanceFactoryContext factoryContext,
			ExtensionContext extensionContext
	) throws TestInstantiationException {
		WireRepository repository = ExtensionCache.getOrCreate(extensionContext);

		return repository.tryGet((Class<Object>) factoryContext.getTestClass())
				.orElseGet(() -> repository.onDemandInjector().get(factoryContext.getTestClass()));
	}

	@Override
	public boolean supportsParameter(
			ParameterContext parameterContext,
			ExtensionContext extensionContext
	) throws ParameterResolutionException {
		TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
		WireRepository repository = ExtensionCache.getOrCreate(extensionContext);

		return repository.contains(typeIdentifier);
	}

	@Override
	public Object resolveParameter(
			ParameterContext parameterContext,
			ExtensionContext extensionContext
	) throws ParameterResolutionException {
		TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
		WireRepository repository = ExtensionCache.getOrCreate(extensionContext);

		if (typeIdentifier.isNativeProvider()) {
			return repository.getNativeProvider(typeIdentifier.getGenericTypes().getFirst());
		} else if(typeIdentifier.isBean()) {
			return repository.getBean(typeIdentifier.getGenericTypes().getFirst());
		} else {
			return repository.get(typeIdentifier);
		}
	}
}
