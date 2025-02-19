package com.wiredi.runtime;

import com.wiredi.runtime.beans.Bean;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An ObjectReference is a lacy resolver of the provided type.
 * <p>
 * It holds a reference to the WireRepository, as well as the requested type.
 * Basically, this class is a utility to resolve instances of a class on demand, like this:
 *
 * <pre><code>
 * public class Example {
 *     private final ObjectReference&#60;MyDependency&#62; reference;
 *
 *     public Example(ObjectReference&#60;MyDependency&#62; reference) {
 *          this.reference = reference;
 *     }
 *
 *     public void myOperation() {
 *          // Consume the object without requiring the instance
 *          reference.ifAvailable(it -> {
 *              // Some logic with the instance...
 *          });
 *          // Get the instance
 *          MyDependency dependency = reference.getInstance();
 *          // Get all available instance
 *          List&#60;MyDependency&#62; allInstances = reference.getAll();
 *     }
 * }
 * </code></pre>
 * <p>
 * The main purpose of this is to be used while using injections and to resolve beans only if required.
 * If (for example) you'd like to only execute a bean instance if a feature flag is set to true and the bean is set,
 * this ObjectReference could be used like this:
 *
 * <pre><code>
 * public void myConfiguration() {
 *      ObjectReference&#60;MyDependency&#62; reference = ...;
 *      if (featureFlags.isEnabled()) {
 *          reference.ifAvailable(it -> it.runBusinessCase());
 *      }
 * }
 * </code></pre>
 *
 * @param <T>
 */
public class ObjectReference<T> {

    private final Bean<T> bean;
    private final WireRepository wireRepository;
    private final TypeIdentifier<T> type;

    public ObjectReference(WireRepository wireRepository, TypeIdentifier<T> type) {
        this.wireRepository = wireRepository;
        this.type = type;
        this.bean = wireRepository.getBean(type);
    }

    public WireRepository wireRepository() {
        return wireRepository;
    }

    public Bean<T> bean() {
        return bean;
    }

    public List<T> getAll() {
        return wireRepository.getAll(type);
    }

    @Nullable
    public T getInstance() {
        if (bean.isEmpty()) {
            return null;
        }
        List<T> allInstances = wireRepository.getAll(type);
        if (allInstances.isEmpty()) {
            return null;
        }

        if (allInstances.size() == 1) {
            return allInstances.getFirst();
        }

        if (bean.getAllUnqualified().size() == 1) {
            return bean.getAllUnqualified().getFirst().get(wireRepository, type);
        }

        return null;
    }

    @NotNull
    public T getInstance(Supplier<@NotNull T> defaultValue) {
        return Objects.requireNonNullElseGet(getInstance(), defaultValue);
    }

    public void ifAvailable(Consumer<? super T> consumer) {
        T instance = getInstance();
        if (instance != null) {
            consumer.accept(instance);
        }
    }
}
