package com.wiredi.runtime.beans;

import com.wiredi.runtime.beans.value.BeanValue;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;

import java.util.List;
import java.util.function.Supplier;

public interface Bean<T> {

    static <T> Bean<T> empty() {
        return UnmodifiableBean.empty();
    }

    int size();

    TypeIdentifier<T> rootType();

    List<IdentifiableProvider<T>> getAll();

    List<IdentifiableProvider<T>> getAll(TypeIdentifier<T> concreteType);

    List<IdentifiableProvider<T>> getAllUnqualified();

    List<IdentifiableProvider<T>> getAllUnqualified(TypeIdentifier<T> concreteType);

    List<IdentifiableProvider<T>> getAllQualified();

    List<IdentifiableProvider<T>> getAllQualified(TypeIdentifier<T> typeIdentifier);

    BeanValue<T> get(QualifierType qualifierType);

    BeanValue<T> get(TypeIdentifier<T> concreteType, Supplier<WireConflictResolver> conflictResolver);

    /**
     * Returns the value of the Bean.
     * <p>
     * It follows a similar algorithm as {@link #get(TypeIdentifier, Supplier)}.
     * <p>
     * If a primary bean can be found, return it.
     * If no primary bean is found, this will only return a value if exactly one instance is maintained in this bean.
     *
     * @param conflictResolver
     * @return
     */
    BeanValue<T> get(Supplier<WireConflictResolver> conflictResolver);

    boolean isEmpty();

    default boolean isNotEmpty() {
        return !isEmpty();
    }
}
