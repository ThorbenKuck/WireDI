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

    /**
     * Returns the wire repository associated with this reference.
     * <p>
     * The wire repository is used for resolving bean instances.
     *
     * @return the wire repository instance used by this reference
     */
    public WireRepository wireRepository() {
        return wireRepository;
    }

    /**
     * Returns the bean definition associated with this reference.
     * <p>
     * The bean contains metadata about how to create and manage instances.
     *
     * @return the bean definition for the referenced type
     */
    public Bean<T> bean() {
        return bean;
    }

    /**
     * Returns all available instances of the referenced type.
     * <p>
     * This method will resolve all beans of the specified type from the wire repository.
     *
     * @return a list containing all available instances of the referenced type
     */
    public List<T> getAll() {
        return wireRepository.getAll(type);
    }

    /**
     * Attempts to get a single instance of the referenced type.
     * <p>
     * Returns null in the following cases:
     * - If the bean definition is empty
     * - If no instances are available
     * - If multiple qualified instances exist with no clear primary candidate
     * <p>
     * If multiple instances exist but only one is unqualified, that instance will be returned.
     *
     * @return a single instance if available and unambiguous, null otherwise
     */
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

    /**
     * Gets an instance of the referenced type, falling back to the provided supplier.
     * <p>
     * If no instance is available through the wire repository.
     *
     * @param defaultValue supplier to provide a default value if no instance is available
     * @return the instance from the repository if available, otherwise the result of the defaultValue supplier
     * @throws NullPointerException if both the instance and the supplied default value are null
     */
    @NotNull
    public T getInstance(Supplier<@NotNull T> defaultValue) {
        return Objects.requireNonNullElseGet(getInstance(), defaultValue);
    }

    /**
     * Executes the provided consumer with an instance if one is available.
     * <p>
     * If no instance is available, the consumer will not be executed.
     * This method provides a safe way to work with optional dependencies.
     *
     * @param consumer the consumer to execute with the instance if available
     */
    public void ifAvailable(Consumer<? super T> consumer) {
        @Nullable T instance = getInstance();
        if (instance != null) {
            consumer.accept(instance);
        }
    }

    /**
     * Executes the provided consumer with an instance of the specified type if one is available
     * and is assignable to the given class.
     *
     * This method provides type-safe access to the
     * referenced object when a specific subtype is needed.
     *
     * @param clazz    the Class object representing the desired type
     * @param consumer the consumer to execute with the instance if available and of the correct type
     * @param <S>      the type parameter for the desired class
     * @return always returns null (method exists for compatibility with existing patterns)
     */
    @Nullable
    public <S> S ifAvailable(Class<S> clazz, Consumer<? super S> consumer) {
        @Nullable T instance = getInstance();
        if (instance != null && clazz.isAssignableFrom(instance.getClass())) {
            consumer.accept(clazz.cast(instance));
        }
        return null;
    }
}
