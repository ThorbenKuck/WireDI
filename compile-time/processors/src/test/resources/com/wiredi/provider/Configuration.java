package com.wiredi.provider;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.domain.provider.TypeIdentifier;

@Wire(proxy = false)
public class Configuration {
    @Provider
    public Interface implementation(TypeIdentifier<Interface> concreteType) {
        return new Implementation();
    }
}
