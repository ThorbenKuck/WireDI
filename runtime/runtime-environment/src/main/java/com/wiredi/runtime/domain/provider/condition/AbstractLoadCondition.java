package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionContext;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.WireRepository;

import java.util.Optional;

public abstract class AbstractLoadCondition implements LoadCondition {

    protected boolean evaluate(WireRepository wireRepository, Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        return wireRepository.tryGet((Class<ConditionEvaluator>) evaluatorType)
                .or(() -> Optional.ofNullable(wireRepository.onDemandInjector().get(evaluatorType)))
                .map(it -> it.matches(
                        new ConditionContext(
                                wireRepository.environment(),
                                wireRepository.beanContainer(),
                                annotationMetaData
                        )
                ))
                .orElse(false);
    }
}
