package com.wiredi.runtime;

import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.exceptions.DiInstantiationException;
import com.wiredi.runtime.exceptions.DiLoadingException;
import com.wiredi.runtime.lang.Ordered;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A catalog that is used when initializing a {@link WireContainer}
 */
public class ProviderCatalog {

    private final List<IdentifiableProvider<?>> registeredProviders = new ArrayList<>();
    private final List<ProviderScope> conditionalProviders = new ArrayList<>();
    private final Map<IdentifiableProvider<?>, List<Throwable>> errors = new ConcurrentHashMap<>();
    private final ProviderCatalogErrorReporter errorReporter;

    public ProviderCatalog(ProviderCatalogErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    public ProviderCatalog() {
        this.errorReporter = new ProviderCatalogErrorReporter.Default();
    }

    public <T> void noteError(@NotNull IdentifiableProvider<T> t, @NotNull Throwable throwable) {
        errors.computeIfAbsent(t, it -> new ArrayList<>()).add(throwable);
    }

    public void addSuccessfullyRegisteredProvider(IdentifiableProvider<?> provider) {
        conditionalProviders.remove(provider);
        this.registeredProviders.add(provider);
    }

    public void addConditionalProvider(IdentifiableProvider<?> provider, Scope scope) {
        this.conditionalProviders.add(new ProviderScope(provider, scope));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public DiLoadingException printErrors() {
        errorReporter.report(errors);
        DiLoadingException diLoadingException = new DiLoadingException("Error while loading providers: " + errors.size() + " errors occurred.");
        errors.forEach((provider, throwables) -> {
            throwables.forEach(diLoadingException::addSuppressed);
        });

        return diLoadingException;
    }

    public List<ProviderScope> conditionalProviders() {
        return Collections.unmodifiableList(conditionalProviders);
    }

    public int countRegisteredProviders() {
        return registeredProviders.size();
    }

    public record ProviderScope(
            @NotNull IdentifiableProvider<?> provider,
            @NotNull Scope scope
    ) implements Ordered {

        public void register() {
            scope.register(provider);
        }

        @Override
        public int getOrder() {
            return provider.getOrder();
        }
    }
}
