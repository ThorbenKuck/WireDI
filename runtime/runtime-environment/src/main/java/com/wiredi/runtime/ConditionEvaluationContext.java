package com.wiredi.runtime;

import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.lang.Counter;

public record ConditionEvaluationContext(
        ProviderCatalog providerCatalog,
        Counter appliedConditionalProviders,
        Counter additionalRounds,
        Integer conditionalRoundThreshold,
        ConditionEvaluation conditionEvaluation,
        WireContainer wireContainer
) {
}
