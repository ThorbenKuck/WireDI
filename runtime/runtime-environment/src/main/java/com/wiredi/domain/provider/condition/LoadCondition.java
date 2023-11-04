package com.wiredi.domain.provider.condition;

import com.wiredi.runtime.WireRepository;

public interface LoadCondition {

    boolean matches(WireRepository wireRepository);

    LoadCondition TRUE = new ConstantLoadCondition(true);

    LoadCondition FALSE = new ConstantLoadCondition(false);

}
