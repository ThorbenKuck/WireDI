package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.Conditional;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SingleLoadCondition implements LoadCondition {

    private final AnnotationMetadata annotationMetaData;
    private final Class<? extends ConditionEvaluator> evaluatorType;

    public SingleLoadCondition(Class<? extends ConditionEvaluator> evaluatorType) {
        this(evaluatorType, AnnotationMetadata.builder(Conditional.class.getSimpleName())
                .withField("value", evaluatorType)
                .build());
    }

    public SingleLoadCondition(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetadata annotationMetaData) {
        this.annotationMetaData = annotationMetaData;
        this.evaluatorType = evaluatorType;
    }

    @Override
    public void test(ConditionEvaluation.Context context) {
        ConditionEvaluator evaluator = context.get(evaluatorType);
        context.withAnnotationMetadata(annotationMetaData, evaluator::test);
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetadata annotationMetaData) {
        return new BatchLoadCondition(
                List.of(
                        new LoadConditionEvaluationStage(this.evaluatorType, this.annotationMetaData),
                        new LoadConditionEvaluationStage(evaluatorType, annotationMetaData)
                )
        );
    }

    @Override
    public String toString() {
        return "Condition(" +
                "metadata=" + annotationMetaData +
                ", evaluatorType=" + evaluatorType +
                ')';
    }
}
