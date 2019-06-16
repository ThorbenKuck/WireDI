package com.github.thorbenkuck.di;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class WiredTypes implements Repository {

	private static final Map<Class<?>, IdentifiableProvider<?>> mapping = new HashMap<>();
	private static boolean loaded;
	private static final boolean autoLoad = DiProperties.doAutoLoad();

	private static void process(IdentifiableProvider provider) {
		for (Object wiredType : provider.wiredTypes()) {
			if(wiredType == null) {
				throw new DiLoadingException("The provider " + provider + " returned null as a identifiable type! This is not permitted.\n" +
						"If you did not create your own instance, please submit your annotated class to github.");
			}
			mapping.put((Class<?>) wiredType, provider);
		}
	}

	private void instantiateNonLazy() {
		for(IdentifiableProvider provider : mapping.values()) {
			if(!provider.lazy()) {
				try {
					provider.instantiate(this);
				} catch (Exception e) {
					throw new DiLoadingException("Error while instantiating " + provider, e);
				}
			}
		}
	}

	private void clearLoadedInstances() {
		synchronized (mapping) {
			mapping.clear();
			loaded = false;
		}
	}

	private void loadFromServiceFile() {
		synchronized (mapping) {
			if(loaded) {
				// Ignore this call. Important
				// so that we do not override
				// the cached instance, which
				// may already be in use.
				return;
			}
			// automatically load the stored
			// instances and mark this class
			// as loaded. Done to prevent manual
			// reloads (should not be done)
			ServiceLoader<IdentifiableProvider> serviceLoader = ServiceLoader.load(IdentifiableProvider.class);
			serviceLoader.forEach(WiredTypes::process);
			loaded = true;
		}
	}

	public WiredTypes() {
		if(autoLoad) {
			load();
		}
	}

	public void unload() {
		clearLoadedInstances();
	}

	public void load() {
		if(loaded) {
			return;
		}
		loadFromServiceFile();
		synchronized (mapping) {
			mapping.put(Repository.class, new RepositoryIdentifyingProvider(this));
		}
		instantiateNonLazy();
	}

	@Override
	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		IdentifiableProvider<T> provider;

		synchronized (mapping) {
			provider = (IdentifiableProvider<T>) mapping.get(type);
		}

		if(provider == null) {
			return null;
		}

		Object t;
		try {
			provider.instantiate(this);
			t = provider.get();
		} catch (Exception e) {
			throw new DiInstantiationException("Error while letting the provider " + provider + " produce the correlating instance", e);
		}

		if(t == null) {
			throw new DiInstantiationException("Provider produced null. This is not allowed by design!");
		}

		if(!provider.type().isAssignableFrom(t.getClass())) {
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

		if(provider == null) {
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
