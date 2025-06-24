package com.wiredi.runtime.domain.factories;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;

import java.util.HashMap;
import java.util.Map;

public class BeanFactoryRegistry {

    private final Map<TypeIdentifier, BeanFactory> factories = new HashMap<>();

    public void register(TypeIdentifier type, BeanFactory factory) {
        if (factories.containsKey(type)) {
            throw new IllegalStateException("Type " + type + " already registered");
        }
        factories.put(type, factory);
    }

    public BeanFactory get(TypeIdentifier type) {
        BeanFactory factory = factories.get(type);
        if (factory == null) {
            return BeanFactory.empty();
        }

        return factory;
    }
}
