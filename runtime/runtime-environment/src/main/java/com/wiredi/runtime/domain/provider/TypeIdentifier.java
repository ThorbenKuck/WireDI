package com.wiredi.runtime.domain.provider;

import com.google.common.primitives.Primitives;
import com.wiredi.runtime.beans.Bean;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.wiredi.runtime.lang.Preconditions.is;

/**
 * This class represents a specific type.
 * <p>
 * It is specifically designed to allow for generic type representations, which is only sporadically possible in java
 * due to type erasure.
 * <p>
 * <h2>Using this class</h2>
 * <p>
 * To use this class, you can create an instance using the {@link #of(Class)} method if you are unsure of the exact
 * type, or {@link #just(Class)} if you are 100% certain that the provided class is not a primitive one. Then you
 * can freely append generics utilizing the {@link #withGeneric(Class)} method. For example like this:
 *
 * <pre><code>
 * TypeIdentifier&lt;List&lt;String>> = TypeIdentifier.just(List.class)
 *                                        .withGeneric(String.class)
 * </code></pre>
 * <p>
 * This also works with nested TypeIdentifiers to represent complex types, like this:
 *
 * <pre><code>
 * TypeIdentifier&lt;List&lt;Consumer&lt;String>>> = TypeIdentifier.just(List.class)
 *                                                  .withGeneric(
 *                                                      TypeIdentifier.just(Consumer.class)
 *                                                                      .withGeneric(String.class)
 *                                                  )
 * </code></pre>
 *
 * <h2>Equality</h2>
 * <p>
 * A TypeIdentifier has a special equality rule: <pre>A less specific TypeIdentifier is equal to a more specific TypeIdentifier</pre>.
 * This rule means that the following is True:
 *
 * <pre><code>
 * TypeIdentifier&lt;List&lt;String>> specific = ...
 * TypeIdentifier&lt;List> general = ...
 *
 * assert general.equals(specific) // Passes, no error thrown
 * assert specific.equals(general) // Fails, specific is not equal to general
 * </code></pre>
 * <p>
 * This helps with general searches. If you want to find a very specific class, you can construct a very specific TypeIdentifier
 * whilst a more broad TypeIdentifier yields broader results.
 * <p>
 * <h2>Type Safety</h2>
 * <p>
 * This classes uses the class name for type comparisons instead of the class directly. This ensures, that the same
 * class can be combined across different class loaders.
 *
 * @param <T> The generic that this TypeIdentifier represents
 */
public class TypeIdentifier<T> {

    public static final TypeIdentifier<Object> OBJECT = new TypeIdentifier<>(Object.class);
    public static final TypeIdentifier<Integer> INTEGER = new TypeIdentifier<>(Integer.class);
    public static final TypeIdentifier<Long> LONG = new TypeIdentifier<>(Long.class);
    public static final TypeIdentifier<Float> FLOAT = new TypeIdentifier<>(Float.class);
    public static final TypeIdentifier<Double> DOUBLE = new TypeIdentifier<>(Double.class);
    public static final TypeIdentifier<Character> CHAR = new TypeIdentifier<>(Character.class);
    public static final TypeIdentifier<String> STRING = new TypeIdentifier<>(String.class);
    public static final TypeIdentifier<UUID> UUID = new TypeIdentifier<>(UUID.class);
    public static final TypeIdentifier<BigDecimal> BIG_DECIMAL = new TypeIdentifier<>(BigDecimal.class);
    public static final TypeIdentifier<BigInteger> BIG_INTEGER = new TypeIdentifier<>(BigInteger.class);
    public static final TypeIdentifier<LocalDate> LOCAL_DATE = new TypeIdentifier<>(LocalDate.class);
    public static final TypeIdentifier<LocalDateTime> LOCAL_DATE_TIME = new TypeIdentifier<>(LocalDateTime.class);
    public static final TypeIdentifier<ZonedDateTime> ZONED_DATE_TIME = new TypeIdentifier<>(ZonedDateTime.class);
    public static final TypeIdentifier<OffsetDateTime> OFFSET_DATE_TIME = new TypeIdentifier<>(OffsetDateTime.class);
    public static final TypeIdentifier<ZoneOffset> ZONE_OFFSET = new TypeIdentifier<>(ZoneOffset.class);

    @NotNull
    private final Class<T> rootType;
    @NotNull
    private final List<TypeIdentifier<?>> genericTypes = new ArrayList<>();

    private TypeIdentifier(@NotNull Class<T> rootType) {
        this.rootType = rootType;
    }

    /**
     * Creates a new TypeIdentifier for the given type.
     * <p>
     * This method behaves like {@link #of(Class)}, but
     *
     * @param type
     * @param <T>
     * @return
     */
    @NotNull
    public static <T> TypeIdentifier<T> just(@NotNull Class<T> type) {
        return new TypeIdentifier<>(type);
    }

    @NotNull
    public static <T> TypeIdentifier<T> of(@NotNull Class<T> type) {
        if (type.isPrimitive()) {
            return new TypeIdentifier<>(Primitives.wrap(type));
        }
        return new TypeIdentifier<>(type);
    }

    /**
     * This method is resolving a {@link Type java.lang.Type}.
     * <p>
     * In general, this has a higher computational complexity, so it should be used sparingly, preferably only in tests.
     *
     * @param type the type to use
     * @param <T>  the generic type, that is defined by the caller. The implementation trusts that this matches with the type.
     * @return a new TypeIdentifier for the provided Type
     * @throws IllegalArgumentException if the Type could not be resolved
     */
    @NotNull
    public static <T> TypeIdentifier<T> of(@NotNull Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            TypeIdentifier<T> result = new TypeIdentifier<>((Class<T>) parameterizedType.getRawType());
            for (Type argument : parameterizedType.getActualTypeArguments()) {
                result.withGeneric(TypeIdentifier.of(argument));
            }

            return result;
        }

        if (type instanceof Class<?>) {
            return of((Class<T>) type);
        }

        throw new IllegalArgumentException("Unsupported type to construct a TypeIdentifier: " + type);
    }

    @NotNull
    public TypeIdentifier<T> erasure() {
        if (genericTypes.isEmpty()) {
            return this;
        } else {
            return TypeIdentifier.of(rootType);
        }
    }

    public boolean willErase() {
        return !genericTypes.isEmpty();
    }

    @NotNull
    public static <T> TypeIdentifier<? extends T> resolve(@NotNull T instance) {
        return of((Class<T>) instance.getClass());
    }

    @NotNull
    public <S extends T> TypeIdentifier<S> withGeneric(@NotNull Class<?> type) {
        return withGeneric(TypeIdentifier.of(type));
    }

    @NotNull
    public <S extends T> TypeIdentifier<S> withWildcard() {
        return withGeneric(TypeIdentifier.OBJECT);
    }


    @NotNull
    public <S extends T> TypeIdentifier<S> withGeneric(@NotNull TypeIdentifier<?> type) {
        genericTypes.add(type);

        return (TypeIdentifier<S>) this;
    }

    public boolean isAssignableFrom(TypeIdentifier<?> typeIdentifier) {
        if (!rootType.isAssignableFrom(typeIdentifier.rootType)) {
            return false;
        }

        if (typeIdentifier.genericTypes.size() > genericTypes.size()) {
            return false;
        }
        for (int i = 0; i < typeIdentifier.genericTypes.size(); i++) {
            TypeIdentifier<?> generic = genericTypes.get(i);
            TypeIdentifier<?> otherGeneric = typeIdentifier.genericTypes.get(i);
            if (otherGeneric != null) {
                if (!generic.isAssignableFrom(otherGeneric)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isInstanceOf(TypeIdentifier<?> typeIdentifier) {
        if (!typeIdentifier.rootType.isAssignableFrom(rootType)) {
            return false;
        }

        if (typeIdentifier.genericTypes.size() > genericTypes.size()) {
            return false;
        }
        for (int i = 0; i < typeIdentifier.genericTypes.size(); i++) {
            TypeIdentifier<?> generic = genericTypes.get(i);
            TypeIdentifier<?> otherGeneric = typeIdentifier.genericTypes.get(i);
            if (otherGeneric != null) {
                if (!generic.isInstanceOf(otherGeneric)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isAssignableFrom(Class<?> type) {
        return rootType.isAssignableFrom(type);
    }

    public boolean isNativeProvider() {
        return isAssignableFrom(IdentifiableProvider.class) && getGenericTypes().size() == 1;
    }

    public boolean isBean() {
        return isAssignableFrom(Bean.class) && getGenericTypes().size() == 1;
    }

    public boolean referencesBeanType() {
        return isNativeProvider() || isBean();
    }

    public boolean referenceConcreteType() {
        return !referencesBeanType();
    }

    public <S> TypeIdentifier<S> firstGenericType() {
        is(!genericTypes.isEmpty(), () -> "There are no generics set on " + this);
        return (TypeIdentifier<S>) genericTypes.get(0);
    }

    public <S> Class<S> firstGeneric() {
        is(!genericTypes.isEmpty(), () -> "There are no generics set on " + this);
        return (Class<S>) genericTypes.get(0).rootType;
    }

    @NotNull
    public Class<T> getRootType() {
        return rootType;
    }

    @NotNull
    public List<TypeIdentifier<?>> getGenericTypes() {
        return genericTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeIdentifier<?> that = (TypeIdentifier<?>) o;

        if (!Objects.equals(rootType.getName(), that.rootType.getName())) {
            return false;
        }

        if (genericTypes.size() > that.genericTypes.size()) {
            return false;
        }

        for (int index = 0; index < genericTypes.size(); index++) {
            TypeIdentifier<?> current = genericTypes.get(index);
            TypeIdentifier<?> other = that.genericTypes.get(index);
            if (!current.equals(other)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootType, genericTypes);
    }

    @Override
    public String toString() {
        if (genericTypes.isEmpty()) {
            return rootType.getName();
        }

        return rootType.getName() + "<" + genericTypes.stream().map(TypeIdentifier::toString).collect(Collectors.joining(", ")) + ">";
    }
}
