package com.wiredi.processor.tck;

import com.wiredi.domain.conditional.ConditionContext;
import com.wiredi.domain.conditional.ConditionEvaluator;
import org.jetbrains.annotations.NotNull;

public class TrueConditionEvaluator implements ConditionEvaluator {
    @Override
    public boolean matches(@NotNull ConditionContext context) {
        return true;
    }
}
