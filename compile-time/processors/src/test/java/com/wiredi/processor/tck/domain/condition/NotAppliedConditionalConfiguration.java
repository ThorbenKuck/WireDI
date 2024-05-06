package com.wiredi.processor.tck.domain.condition;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;

@Wire
public class NotAppliedConditionalConfiguration {
    @Provider
    @ConditionalOnMissingBean(type = ConditionalClass.class)
    public ConditionalClass conditionalClass() {
        return new ShouldNotBeTaken();
    }
}
