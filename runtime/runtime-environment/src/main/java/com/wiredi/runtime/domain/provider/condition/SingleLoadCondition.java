package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.conditional.Conditional;

import java.util.List;

public class SingleLoadCondition extends AbstractLoadCondition {

    private final AnnotationMetaData annotationMetaData;
    private final Class<? extends ConditionEvaluator> evaluatorType;

    public SingleLoadCondition(Class<? extends ConditionEvaluator> evaluatorType) {
        this(evaluatorType, AnnotationMetaData.newInstance(Conditional.class.getSimpleName())
                .withField("value", evaluatorType)
                .build());
    }

    public SingleLoadCondition(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        this.annotationMetaData = annotationMetaData;
        this.evaluatorType = evaluatorType;
    }

    @Override
    public boolean matches(WireRepository wireRepository) {
        return evaluate(wireRepository, evaluatorType, annotationMetaData);
    }

    @Override
    public LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
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
                "annotationMetaData=" + annotationMetaData +
                ", evaluatorType=" + evaluatorType +
                ')';
    }
}
