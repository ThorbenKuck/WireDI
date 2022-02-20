package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.ProxyFactory;
import com.github.thorbenkuck.di.aspects.AspectRepository;

import java.util.List;
import java.util.stream.Collectors;

public class Proxies extends SynchronizedServiceLoader<ProxyFactory> {

    private final AspectRepository aspectRepository;

    public Proxies(AspectRepository aspectRepository) {
        this.aspectRepository = aspectRepository;
    }

    public static boolean isProxy(Object instance) {
        return false;
    }

    @Override
    public Class<ProxyFactory> serviceType() {
        return ProxyFactory.class;
    }

    public <T> T tryWrap(T instance, Class<T> type, WiredTypes types) {
        List<ProxyFactory<T>> proxyFactories = getAll(type);
        if (proxyFactories.isEmpty()) {
            return instance;
        }

        ProxyFactory<T> proxyFactory = findPrimaryProvider(proxyFactories, type);
        return proxyFactory.wrap(instance, aspectRepository, types);
    }

    private <T> ProxyFactory<T> findPrimaryProvider(List<ProxyFactory<T>> providers, Class<T> type) {
        if(providers.size() == 1) {
            return providers.get(0);
        } else {
            throw new IllegalArgumentException("There have been multiple registered Facets for the type " + type);
        }
    }

    private <T> List<ProxyFactory<T>> getAll(Class<T> type) {
        return dataAccess.read(() -> unsafeGet(type)
                .stream().map(it -> (ProxyFactory<T>) it)
                .collect(Collectors.toList()));
    }
}
