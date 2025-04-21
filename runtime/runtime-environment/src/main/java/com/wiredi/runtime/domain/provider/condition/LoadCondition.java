package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.Conditional;
import org.jetbrains.annotations.NotNull;

public interface LoadCondition {

    boolean matches(WireRepository wireRepository);

    static LoadCondition just(Class<? extends ConditionEvaluator> evaluatorType) {
        return new SingleLoadCondition(evaluatorType);
    }

    static LoadCondition just(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        return new SingleLoadCondition(evaluatorType, annotationMetaData);
    }

    /**
     * Creates a new builder for a condition using the specified evaluator.
     *
     * @param evaluatorClass the condition evaluator class
     * @return a new builder instance
     */
    static Builder of(Class<? extends ConditionEvaluator> evaluatorClass) {
        return new Builder(evaluatorClass);
    }

    /**
     * Creates a new builder for a condition using the specified evaluator.
     *
     * @param evaluatorClass the condition evaluator class
     * @return a new builder instance
     */
    static LoadCondition of(Class<? extends ConditionEvaluator> evaluatorClass, AnnotationMetaData annotationMetaData) {
        return new Builder(evaluatorClass).withAnnotation(annotationMetaData).build();
    }

    /**
     * Creates a new builder for a condition using the specified evaluator.
     *
     * @param evaluatorClass the condition evaluator class
     * @return a new builder instance
     */
    static LoadCondition of(Class<? extends ConditionEvaluator> evaluatorClass, AnnotationMetaData.Builder annotationMetaDataBuilder) {
        return new Builder(evaluatorClass).withAnnotation(annotationMetaDataBuilder.build()).build();
    }

    default LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType) {
        return add(evaluatorType, AnnotationMetaData.newInstance(Conditional.class.getSimpleName())
                .withField("value", evaluatorType)
                .build());
    }

    default LoadCondition and(LoadCondition other) {
        return new AllLoadCondition(this, other);
    }

    default LoadCondition or(LoadCondition other) {
        return new AnyLoadCondition(this, other);
    }

    LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData);

    LoadCondition TRUE = new ConstantLoadCondition(true);

    LoadCondition FALSE = new ConstantLoadCondition(false);

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

        private final Class<? extends ConditionEvaluator> evaluatorClass;
        private AnnotationMetaData annotation;
        private LoadCondition baseCondition;

        private Builder(Class<? extends ConditionEvaluator> evaluatorClass) {
            this.evaluatorClass = evaluatorClass;
            this.baseCondition = null;
        }

        /**
         * Creates a new builder for a condition using the specified evaluator.
         *
         * @param evaluatorClass the condition evaluator class
         * @return a new builder instance
         */
        public static Builder forEvaluator(Class<? extends ConditionEvaluator> evaluatorClass) {
            return new Builder(evaluatorClass);
        }

        /**
         * Adds a field to the annotation metadata.

         * @return this builder
         */
        public Builder withAnnotation(AnnotationMetaData annotation) {
            this.annotation = annotation;
            return this;
        }

        /**
         * Adds a field to the annotation metadata.

         * @return this builder
         */
        public Builder withAnnotation(AnnotationMetaData.Builder annotationBuilder) {
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
            if (baseCondition != null) {
                return baseCondition;
            }

            if (evaluatorClass == null) {
                throw new IllegalStateException("Either evaluator class or base condition must be provided");
            }

            if (annotation == null) {
                return new SingleLoadCondition(evaluatorClass);
            } else {
                return new SingleLoadCondition(evaluatorClass, annotation);
            }
        }
    }
}
