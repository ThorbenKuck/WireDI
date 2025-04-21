package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A simple implementation of {@link IdentifiableProvider} that can be configured programmatically.
 * <p>
 * This provider is particularly useful for testing or for programmatically creating providers
 * without requiring annotations. It supports all features of regular providers including
 * conditions, qualifiers, and custom type identifiers.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create a simple provider for MyService
 * SimpleProvider<MyService> provider = SimpleProvider.builder(MyService.class)
 *     .withInstance(() -> new MyService())
 *     .withQualifier("primary")
 *     .withOrder(100)
 *     .build();
 *
 * // Create a conditional provider
 * SimpleProvider<MyService> conditionalProvider = SimpleProvider.builder(MyService.class)
 *     .withInstance(() -> new MyService())
 *     .withCondition(LoadConditionBuilder.forEvaluator(ConditionalOnClassEvaluator.class)
 *         .withField("className", "com.example.SomeClass")
 *         .build())
 *     .build();
 * }</pre>
 *
 * @param <T> the type of the provided instance
 */
public class SimpleProvider<T> implements IdentifiableProvider<T> {

    private final TypeIdentifier<T> type;
    private final List<TypeIdentifier<?>> additionalTypes;
    private final List<QualifierType> qualifiers;
    private final Supplier<T> instanceSupplier;
    private final LoadCondition condition;
    private final int order;
    private final boolean singleton;
    private final boolean primary;

    private SimpleProvider(Builder<T> builder) {
        this.type = builder.type;
        this.additionalTypes = List.copyOf(builder.additionalTypes);
        this.qualifiers = List.copyOf(builder.qualifiers);
        this.instanceSupplier = builder.instanceSupplier;
        this.condition = builder.condition;
        this.order = builder.order;
        this.singleton = Objects.requireNonNullElse(builder.singleton, false);
        this.primary = builder.primary;
    }

    /**
     * Creates a new builder for a SimpleProvider.
     *
     * @param typeIdentifier the type identifier this provider will provide
     * @param <T>            the type parameter
     * @return a new builder
     */
    public static <T> Builder<T> builder(TypeIdentifier<T> typeIdentifier) {
        return new Builder<>(typeIdentifier);
    }

    /**
     * Creates a new builder for a SimpleProvider.
     *
     * @param type the class type this provider will provide
     * @param <T>  the type parameter
     * @return a new builder
     */
    public static <T> Buildable<T> builder(Class<T> type) {
        return new Builder<>(TypeIdentifier.of(type));
    }

    /**
     * Creates a new builder for a SimpleProvider.
     *
     * @param instance the instance to use
     * @param <T>  the type parameter
     * @return a new builder
     */
    public static <T> Builder<T> builder(T instance) {
        Buildable<T> buildable = (Buildable<T>) builder(instance.getClass());
        return buildable.withInstance(instance);
    }

    @Override
    public @NotNull TypeIdentifier<T> type() {
        return type;
    }

    @Override
    public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
        return additionalTypes;
    }

    @Override
    public @NotNull List<QualifierType> qualifiers() {
        return qualifiers;
    }

    @Override
    public @NotNull T get(@NotNull WireRepository wireRepository) {
        return instanceSupplier.get();
    }

    @Override
    public @Nullable LoadCondition condition() {
        return condition;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    @Override
    public boolean primary() {
        return primary;
    }

    @Override
    public @Nullable T get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<T> concreteType) {
        return instanceSupplier.get();
    }



    public interface Buildable<T> {
        Builder<T> withInstance(Supplier<T> instanceSupplier);
        Builder<T> withInstance(T instance);
        Buildable<T> withAdditionalType(TypeIdentifier<?> additionalType);
        Buildable<T> withAdditionalType(Class<?> additionalType);
        Buildable<T> withAdditionalTypes(Class<?>... additionalType);
        Buildable<T> withAdditionalTypes(TypeIdentifier<?>... additionalType);
        Buildable<T> withAdditionalTypes(List<TypeIdentifier<?>> additionalType);
        Buildable<T> withQualifier(QualifierType qualifier);
        Buildable<T> withQualifier(String qualifier);
        Buildable<T> withOrder(int order);
        Buildable<T> withSingleton(boolean singleton);
        Buildable<T> withPrimary(boolean primary);
        Buildable<T> withCondition(LoadCondition condition);
        Buildable<T> withCondition(Class<? extends ConditionEvaluator> conditionType);
        Buildable<T> withCondition(Class<? extends ConditionEvaluator> conditionType, AnnotationMetaData annotationMetaData);
        Buildable<T> withCondition(Class<? extends ConditionEvaluator> conditionType, Consumer<LoadCondition.Builder> builderConsumer);
    }

    /**
     * Builder for creating SimpleProvider instances.
     *
     * @param <T> the type of the provided instance
     */
    public static class Builder<T> implements Buildable<T> {
        private final TypeIdentifier<T> type;
        private final Set<TypeIdentifier<?>> additionalTypes = new HashSet<>();
        private final Set<QualifierType> qualifiers = new HashSet<>();
        private Supplier<T> instanceSupplier;
        private LoadCondition condition = null;
        private int order = Ordered.DEFAULT;
        private Boolean singleton = null;
        private boolean primary = false;

        private Builder(TypeIdentifier<T> type) {
            this.type = type;
        }

        /**
         * Sets the instance supplier for this provider.
         *
         * @param instanceSupplier the supplier that will create instances
         * @return this builder
         */
        @Override
        public Builder<T> withInstance(Supplier<T> instanceSupplier) {
            if (this.instanceSupplier != null) {
                throw new IllegalStateException("Instance supplier already set");
            }

            this.instanceSupplier = instanceSupplier;
            if (this.singleton == null) {
                this.singleton = false;
            }

            return this;
        }

        /**
         * Sets a fixed instance for this provider.
         *
         * @param instance the instance to provide
         * @return this builder
         */
        @Override
        public Builder<T> withInstance(T instance) {
            if (this.instanceSupplier != null) {
                throw new IllegalStateException("Instance supplier already set");
            }

            this.instanceSupplier = () -> instance;
            if (this.singleton == null) {
                this.singleton = true;
            }

            return this;
        }

        /**
         * Adds an additional type that this provider will wire.
         *
         * @param additionalType the additional type
         * @return this builder
         */
        @Override
        public Builder<T> withAdditionalType(TypeIdentifier<?> additionalType) {
            this.additionalTypes.add(additionalType);
            return this;
        }

        /**
         * Adds an additional type that this provider will wire.
         *
         * @param additionalType the additional type class
         * @return this builder
         */
        @Override
        public Builder<T> withAdditionalType(Class<?> additionalType) {
            return withAdditionalType(TypeIdentifier.of(additionalType));
        }

        /**
         * Adds multiple additional types that this provider will wire.
         *
         * @param additionalType the additional type class
         * @return this builder
         */
        @Override
        public Builder<T> withAdditionalTypes(Class<?>... additionalType) {
            for (Class<?> type : additionalType) {
                withAdditionalType(TypeIdentifier.of(type));
            }

            return this;
        }

        /**
         * Adds an additional type that this provider will wire.
         *
         * @param additionalType the additional type class
         * @return this builder
         */
        @Override
        public Builder<T> withAdditionalTypes(TypeIdentifier<?>... additionalType) {
            for (TypeIdentifier<?> type : additionalType) {
                withAdditionalType(type);
            }

            return this;
        }

        /**
         * Adds an additional type that this provider will wire.
         *
         * @param additionalType the additional type class
         * @return this builder
         */
        @Override
        public Builder<T> withAdditionalTypes(List<TypeIdentifier<?>> additionalType) {
            for (TypeIdentifier<?> type : additionalType) {
                withAdditionalType(type);
            }

            return this;
        }

        /**
         * Adds a qualifier to this provider.
         *
         * @param qualifier the qualifier to add
         * @return this builder
         */
        @Override
        public Builder<T> withQualifier(QualifierType qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        /**
         * Adds a string qualifier to this provider.
         *
         * @param qualifier the qualifier string
         * @return this builder
         */
        @Override
        public Builder<T> withQualifier(String qualifier) {
            return withQualifier(QualifierType.just(qualifier));
        }

        /**
         * Sets the order for this provider.
         *
         * @param order the order value (lower values have higher priority)
         * @return this builder
         */
        @Override
        public Builder<T> withOrder(int order) {
            this.order = order;
            return this;
        }

        /**
         * Sets whether this provider is a singleton.
         *
         * @param singleton true if this provider should provide a singleton instance
         * @return this builder
         */
        @Override
        public Builder<T> withSingleton(boolean singleton) {
            this.singleton = singleton;
            return this;
        }

        /**
         * Sets whether this provider is the primary provider for its {@link #type()} and {@link #additionalWireTypes()}.
         *
         * @param primary true if this provider should be the primary provider
         * @return this builder
         */
        @Override
        public Builder<T> withPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }

        /**
         * Sets a load condition for this provider.
         *
         * @param condition the load condition
         * @return this builder
         */
        @Override
        public Builder<T> withCondition(LoadCondition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Sets a load condition for this provider.
         *
         * @param conditionType the load condition
         * @return this builder
         */
        @Override
        public Builder<T> withCondition(Class<? extends ConditionEvaluator> conditionType) {
            this.condition = LoadCondition.just(conditionType);
            return this;
        }

        /**
         * Sets a load condition for this provider.
         *
         * @param conditionType the load condition
         * @return this builder
         */
        @Override
        public Builder<T> withCondition(Class<? extends ConditionEvaluator> conditionType, AnnotationMetaData annotationMetaData) {
            this.condition = LoadCondition.forEvaluator(conditionType)
                    .withAnnotation(annotationMetaData)
                    .build();
            return this;
        }

        /**
         * Sets a load condition for this provider.
         *
         * @param conditionType the load condition
         * @return this builder
         */
        @Override
        public Builder<T> withCondition(Class<? extends ConditionEvaluator> conditionType, Consumer<LoadCondition.Builder> builderConsumer) {
            LoadCondition.Builder builder = LoadCondition.forEvaluator(conditionType);
            builderConsumer.accept(builder);
            this.condition = builder.build();
            return this;
        }

        /**
         * Builds the SimpleProvider.
         *
         * @return the configured SimpleProvider
         */
        public SimpleProvider<T> build() {
            if (instanceSupplier == null) {
                throw new IllegalStateException("No instance supplier set for SimpleProvider");
            }
            return new SimpleProvider<>(this);
        }
    }
}
