package com.wiredi.domain.provider.condition;

import com.wiredi.runtime.WireRepository;

public class ConstantLoadCondition implements LoadCondition {

    private final boolean condition;

    public ConstantLoadCondition(boolean condition) {
        this.condition = condition;
    }

    @Override
    public final boolean matches(WireRepository wireRepository) {
        return condition;
    }
}
