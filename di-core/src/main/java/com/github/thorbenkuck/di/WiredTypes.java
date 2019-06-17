package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.resources.ResourceRepository;
import com.github.thorbenkuck.di.resources.Resources;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

public class WiredTypes extends SynchronizedServiceLoader<IdentifiableProvider> implements Repository {

	private final Map<Class<?>, IdentifiableProvider<?>> mapping = new HashMap<>();
	private final ResourceRepository resourceRepository = new ResourceRepository();

	public WiredTypes() {
		if (Configuration.doDiAutoLoad()) {
			load();
			resourceRepository.load();
		}
	}

	private void instantiateNonLazy() {
		for (IdentifiableProvider provider : mapping.values()) {
			if (!provider.lazy()) {
				try {
					provider.instantiate(this);
				} catch (Exception e) {
					throw new DiLoadingException("Error while instantiating " + provider, e);
				}
			}
		}
	}

	@Override
	public void add(IdentifiableProvider provider) {
		for (Object wiredType : provider.wiredTypes()) {
			if (wiredType == null) {
				throw new DiLoadingException("The provider " + provider + " returned null as an identifiable type! This is not permitted.\n" +
						"If you did not create your own instance, please submit your annotated class to github.");
			}
			mapping.put((Class<?>) wiredType, provider);
		}
	}

	public void setResource(String key, String value) {
		resourceRepository.set(key, value);
	}

	@Override
	public Resources getResourceRepository() {
		return resourceRepository;
	}

	public void unload() {
		mapping.clear();
		loaded = false;
	}

	@Override
	public void load() {
		super.load();
		resourceRepository.load();
		add(new RepositoryIdentifyingProvider(this));
		instantiateNonLazy();
	}

	@Override
	public Class<IdentifiableProvider> serviceType() {
		return IdentifiableProvider.class;
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		IdentifiableProvider<T> provider;

		synchronized (mapping) {
			provider = (IdentifiableProvider<T>) mapping.get(type);
		}

		if (provider == null) {
			return null;
		}

		Object t;
		try {
			provider.instantiate(this);
			t = provider.get();
		} catch (Exception e) {
			throw new DiInstantiationException("Error while letting the provider " + provider + " produce the correlating instance", e);
		}

		if (t == null) {
			throw new DiInstantiationException("Provider produced null. This is not allowed by design!");
		}

		if (!provider.type().isAssignableFrom(t.getClass())) {
			throw new DiInstantiationException("The provider for the class " + type + " is not compatible with the produced instance " + t);
		}

		return (T) t;
	}

	@Override
	public <T> Provider<T> getProvider(Class<T> type) {
		IdentifiableProvider<T> provider;

		synchronized (mapping) {
			provider = (IdentifiableProvider<T>) mapping.get(type);
		}

		if (provider == null) {
			throw new DiLoadingException("Could not find any provider for the class " + type);
		}

		return new ProviderMapper<>(provider);
	}

	private final class ProviderMapper<T> implements Provider<T> {

		private final IdentifiableProvider<T> provider;

		private ProviderMapper(IdentifiableProvider<T> provider) {
			this.provider = provider;
		}

		@Override
		public T get() {
			return provider.get();
		}
	}
}
