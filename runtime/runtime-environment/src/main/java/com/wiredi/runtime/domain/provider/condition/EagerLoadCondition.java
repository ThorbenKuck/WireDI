package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.Conditional;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EagerLoadCondition implements LoadCondition {

    private final ConditionEvaluator conditionEvaluator;
    private final AnnotationMetaData annotationMetaData;

    public EagerLoadCondition(ConditionEvaluator conditionEvaluator, AnnotationMetaData annotationMetaData) {
        this.conditionEvaluator = conditionEvaluator;
        this.annotationMetaData = annotationMetaData;
    }

    public EagerLoadCondition(ConditionEvaluator conditionEvaluator) {
        this(conditionEvaluator, AnnotationMetaData.builder(Conditional.class.getSimpleName())
                .withField("value", conditionEvaluator.getClass())
                .build());
    }

    @Override
    public boolean matches(@NotNull WireRepository wireRepository) {
        ConditionContext context = ConditionContext.runtime(wireRepository, annotationMetaData);
        conditionEvaluator.test(context);
        return context.isMatched();
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetaData annotationMetaData) {
        return new BatchLoadCondition(
                List.of(
                        new LoadConditionEvaluationStage(this.conditionEvaluator.getClass(), this.annotationMetaData),
                        new LoadConditionEvaluationStage(evaluatorType, annotationMetaData)
                )
        );
    }
}
