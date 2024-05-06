package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionContext;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.Conditional;

import java.util.List;

public class EagerLoadCondition implements LoadCondition {

    private final ConditionEvaluator conditionEvaluator;
    private final AnnotationMetaData annotationMetaData;

    public EagerLoadCondition(ConditionEvaluator conditionEvaluator, AnnotationMetaData annotationMetaData) {
        this.conditionEvaluator = conditionEvaluator;
        this.annotationMetaData = annotationMetaData;
    }

    public EagerLoadCondition(ConditionEvaluator conditionEvaluator) {
        this(conditionEvaluator, AnnotationMetaData.newInstance(Conditional.class.getSimpleName())
                .withField("value", conditionEvaluator.getClass())
                .build());
    }

    @Override
    public boolean matches(WireRepository wireRepository) {
        return conditionEvaluator.matches(new ConditionContext(wireRepository.environment(), wireRepository.beanContainer(), annotationMetaData));
    }

    @Override
    public LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        return new BatchLoadCondition(
                List.of(
                        new LoadConditionEvaluationStage(this.conditionEvaluator.getClass(), this.annotationMetaData),
                        new LoadConditionEvaluationStage(evaluatorType, annotationMetaData)
                )
        );
    }
}
