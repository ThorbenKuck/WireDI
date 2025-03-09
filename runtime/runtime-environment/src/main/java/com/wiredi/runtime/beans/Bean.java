package com.wiredi.runtime.beans;

import com.wiredi.runtime.beans.value.BeanValue;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * This class represents available instances of the provided generic, named Components.
 * <p>
 * A Bean can consist of 0 - n components of the same type.
 * Any IdentifiableProvider that produces the same value is aggregated in one Bean.
 *
 * @param <T>
 */
public interface Bean<T> {

    static <T> Bean<T> empty() {
        return UnmodifiableBean.empty();
    }

    int size();

    @NotNull
    TypeIdentifier<T> rootType();

    @NotNull
    List<IdentifiableProvider<T>> getAll();

    @NotNull
    List<IdentifiableProvider<T>> getAll(TypeIdentifier<T> concreteType);

    @NotNull
    List<IdentifiableProvider<T>> getAllUnqualified();

    @NotNull
    List<IdentifiableProvider<T>> getAllUnqualified(TypeIdentifier<T> concreteType);

    @NotNull
    List<IdentifiableProvider<T>> getAllQualified();

    @NotNull
    List<IdentifiableProvider<T>> getAllQualified(TypeIdentifier<T> typeIdentifier);

    @NotNull
    BeanValue<T> get(QualifierType qualifierType);

    @NotNull
    BeanValue<T> get(TypeIdentifier<T> concreteType, Supplier<WireConflictResolver> conflictResolver);

    /**
     * Returns the value of the Bean.
     * <p>
     * It follows a similar algorithm as {@link #get(TypeIdentifier, Supplier)}.
     * <p>
     * If a primary bean can be found, return it.
     * If no primary bean is found, this method only returns a value if exactly one instance is maintained in this bean.
     *
     * @param conflictResolver the strategy on how to handle conflicts
     * @return a {@link BeanValue} of this Bean
     */
    @NotNull
    BeanValue<T> get(Supplier<WireConflictResolver> conflictResolver);

    boolean isEmpty();

    default boolean isNotEmpty() {
        return !isEmpty();
    }
}
