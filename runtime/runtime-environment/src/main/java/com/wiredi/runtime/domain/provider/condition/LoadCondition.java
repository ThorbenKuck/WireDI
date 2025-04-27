package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.Conditional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LoadCondition {

    LoadCondition TRUE = new ConstantLoadCondition(true);
    LoadCondition FALSE = new ConstantLoadCondition(false);

    @NotNull
    static LoadCondition just(@NotNull Class<? extends ConditionEvaluator> evaluatorType) {
        return new SingleLoadCondition(evaluatorType);
    }

    @NotNull
    static LoadCondition just(
            @NotNull Class<? extends ConditionEvaluator> evaluatorType,
            @NotNull AnnotationMetaData annotationMetaData
    ) {
        return new SingleLoadCondition(evaluatorType, annotationMetaData);
    }

    /**
     * Creates a new builder for a condition using the specified evaluator.
     *
     * @param evaluatorClass the condition evaluator class
     * @return a new builder instance
     */
    @NotNull
    static Builder builder(@NotNull Class<? extends ConditionEvaluator> evaluatorClass) {
        return new Builder(evaluatorClass);
    }

    /**
     * Creates a new builder for a condition using the specified evaluator.
     *
     * @param evaluatorClass the condition evaluator class
     * @return a new builder instance
     */
    @NotNull
    static LoadCondition of(
            @NotNull Class<? extends ConditionEvaluator> evaluatorClass,
            @NotNull AnnotationMetaData annotationMetaData
    ) {
        return builder(evaluatorClass).withAnnotation(annotationMetaData).build();
    }

    /**
     * Creates a new builder for a condition using the specified evaluator.
     *
     * @param evaluatorClass the condition evaluator class
     * @return a new builder instance
     */
    @NotNull
    static LoadCondition of(
            @NotNull Class<? extends ConditionEvaluator> evaluatorClass,
            @NotNull AnnotationMetaData.Builder annotationMetaDataBuilder
    ) {
        return builder(evaluatorClass).withAnnotation(annotationMetaDataBuilder.build()).build();
    }

    boolean matches(@NotNull WireRepository wireRepository);

    @NotNull
    default LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType) {
        return add(evaluatorType, AnnotationMetaData.builder(Conditional.class.getSimpleName())
                .withField("value", evaluatorType)
                .build());
    }

    @NotNull
    default LoadCondition and(@NotNull LoadCondition other) {
        return new AllLoadCondition(this, other);
    }

    @NotNull
    default LoadCondition or(@NotNull LoadCondition other) {
        return new AnyLoadCondition(this, other);
    }

    @NotNull
    LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetaData annotationMetaData);

    /**
     * A builder for creating {@link LoadCondition} instances.
     * <p>
     * This builder provides a fluent API for creating conditions based on {@link ConditionEvaluator}s
     * with appropriate annotation metadata. It can be used to programmatically create conditions
     * that would normally be created through annotations.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Create a condition for the ConditionalOnClass evaluator
     * LoadCondition condition = LoadConditionBuilder.forEvaluator(ConditionalOnClassEvaluator.class)
     *     .withField("className", "com.example.SomeClass")
     *     .build();
     *
     * // Create a combined condition with AND logic
     * LoadCondition combinedCondition = LoadConditionBuilder.forEvaluator(ConditionalOnClassEvaluator.class)
     *     .withField("className", "com.example.ClassA")
     *     .and(
     *         LoadConditionBuilder.forEvaluator(ConditionalOnBeanEvaluator.class)
     *             .withField("type", TypeIdentifier.of(SomeDependency.class))
     *             .build()
     *     )
     *     .build();
     * }</pre>
     */
    class Builder {

        @NotNull
        private final Class<? extends ConditionEvaluator> evaluatorClass;
        @Nullable
        private AnnotationMetaData annotation;

        private Builder(@NotNull Class<? extends ConditionEvaluator> evaluatorClass) {
            this.evaluatorClass = evaluatorClass;
        }

        /**
         * Adds a field to the annotation metadata.
         *
         * @return this builder
         */
        @NotNull
        public Builder withAnnotation(@NotNull AnnotationMetaData annotation) {
            this.annotation = annotation;
            return this;
        }

        /**
         * Adds a field to the annotation metadata.
         *
         * @return this builder
         */
        @NotNull
        public Builder withAnnotation(@NotNull AnnotationMetaData.Builder annotationBuilder) {
            this.annotation = annotationBuilder.build();
            return this;
        }

        /**
         * Builds the load condition.
         *
         * @return the configured load condition
         * @throws IllegalStateException if neither an evaluator class nor a base condition was set
         */
        public @NotNull LoadCondition build() {
            if (annotation == null) {
                return new SingleLoadCondition(evaluatorClass);
            } else {
                return new SingleLoadCondition(evaluatorClass, annotation);
            }
        }
    }
}
