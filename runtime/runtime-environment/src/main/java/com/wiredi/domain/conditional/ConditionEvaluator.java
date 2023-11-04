package com.wiredi.domain.conditional;

import org.jetbrains.annotations.NotNull;

public interface ConditionEvaluator {

    boolean matches(@NotNull final ConditionContext context);
}
