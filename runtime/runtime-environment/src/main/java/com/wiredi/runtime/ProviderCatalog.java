package com.wiredi.runtime;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderCatalog {

    private final List<IdentifiableProvider<?>> registeredProviders = new ArrayList<>();
    private final List<IdentifiableProvider<?>> conditionalProviders = new ArrayList<>();
    private final Map<IdentifiableProvider<?>, List<Throwable>> errors = new ConcurrentHashMap<>();

    public <T> void noteError(@NotNull IdentifiableProvider<T> t, @NotNull Throwable throwable) {
        errors.computeIfAbsent(t, it -> new ArrayList<>()).add(throwable);
    }

    public void addSuccessfullyRegisteredProvider(IdentifiableProvider<?> provider) {
        conditionalProviders.remove(provider);
        this.registeredProviders.add(provider);
    }

    public void addConditionalProvider(IdentifiableProvider<?> provider) {
        this.conditionalProviders.add(provider);
    }

    public List<IdentifiableProvider<?>> conditionalProviders() {
        return Collections.unmodifiableList(conditionalProviders);
    }

    public int countRegisteredProviders() {
        return registeredProviders.size();
    }
}
