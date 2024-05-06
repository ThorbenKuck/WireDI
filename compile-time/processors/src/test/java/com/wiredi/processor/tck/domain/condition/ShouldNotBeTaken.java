package com.wiredi.processor.tck.domain.condition;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;

@Wire
@ConditionalOnMissingBean(type = ConditionalClass.class)
public class ShouldNotBeTaken implements ConditionalClass {
}
