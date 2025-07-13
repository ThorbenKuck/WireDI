package com.wiredi.tests;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.WiredApplicationInstance;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.junit.jupiter.api.extension.*;

public class WireDIExtension implements TestInstanceFactory, TestInstancePreDestroyCallback, ParameterResolver {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(WireDIExtension.class);

	@Override
	public Object createTestInstance(
			TestInstanceFactoryContext factoryContext,
			ExtensionContext extensionContext
	) throws TestInstantiationException {
		WiredApplicationInstance application = ExtensionCache.getOrCreate(extensionContext);
		extensionContext.getStore(NAMESPACE).put(WiredApplicationInstance.class, application);

		return application.wireRepository()
				.tryGet((Class<Object>) factoryContext.getTestClass())
				.orElseGet(() -> application.wireRepository().onDemandInjector().get(factoryContext.getTestClass()));
	}

	@Override
	public void preDestroyTestInstance(ExtensionContext context) {
		ExtensionContext.Store store = context.getStore(NAMESPACE);
		WiredApplicationInstance applicationInstance = store.get(WiredApplicationInstance.class, WiredApplicationInstance.class);
		if (applicationInstance != null) {
			applicationInstance.shutdown();
			store.remove(WiredApplicationInstance.class);
		}
	}

	@Override
	public boolean supportsParameter(
			ParameterContext parameterContext,
			ExtensionContext extensionContext
	) throws ParameterResolutionException {
		TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
		WiredApplicationInstance applicationInstance = ExtensionCache.getOrCreate(extensionContext);

		return applicationInstance.wireRepository().contains(typeIdentifier);
	}

	@Override
	public Object resolveParameter(
			ParameterContext parameterContext,
			ExtensionContext extensionContext
	) throws ParameterResolutionException {
		TypeIdentifier<Object> typeIdentifier = TypeIdentifier.of(parameterContext.getParameter().getParameterizedType());
		WiredApplicationInstance applicationInstance = ExtensionCache.getOrCreate(extensionContext);
		WireContainer repository = applicationInstance.wireRepository();

		if (typeIdentifier.isNativeProvider()) {
			return repository.getNativeProvider(typeIdentifier.getGenericTypes().getFirst());
		} else {
			return repository.get(typeIdentifier);
		}
	}
}
