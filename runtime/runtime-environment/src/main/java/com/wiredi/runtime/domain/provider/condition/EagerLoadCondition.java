package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.Conditional;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EagerLoadCondition implements LoadCondition {

    private final ConditionEvaluator conditionEvaluator;
    private final AnnotationMetadata annotationMetaData;

    public EagerLoadCondition(ConditionEvaluator conditionEvaluator, AnnotationMetadata annotationMetaData) {
        this.conditionEvaluator = conditionEvaluator;
        this.annotationMetaData = annotationMetaData;
    }

    public EagerLoadCondition(ConditionEvaluator conditionEvaluator) {
        this(conditionEvaluator, AnnotationMetadata.builder(Conditional.class.getSimpleName())
                .withField("value", conditionEvaluator.getClass())
                .build());
    }

    @Override
    public void test(ConditionEvaluation.Context context) {
        context.withAnnotationMetadata(annotationMetaData, conditionEvaluator::test);
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetadata annotationMetaData) {
        return new BatchLoadCondition(
                List.of(
                        new LoadConditionEvaluationStage(this.conditionEvaluator.getClass(), this.annotationMetaData),
                        new LoadConditionEvaluationStage(evaluatorType, annotationMetaData)
                )
        );
    }
}
