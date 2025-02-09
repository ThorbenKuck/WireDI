package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;

import java.util.Optional;

public abstract class AbstractLoadCondition implements LoadCondition {

    protected void evaluate(
            WireRepository wireRepository,
            ConditionContext conditionContext,
            Class<? extends ConditionEvaluator> evaluatorType
    ) {
        wireRepository.tryGet((Class<ConditionEvaluator>) evaluatorType)
                .or(() -> Optional.ofNullable(wireRepository.onDemandInjector().get(evaluatorType)))
                .ifPresent(it -> it.test(conditionContext));
    }
}
