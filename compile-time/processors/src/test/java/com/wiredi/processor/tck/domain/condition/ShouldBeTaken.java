package com.wiredi.processor.tck.domain.condition;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;

@Wire
@ConditionalOnMissingBean(type = ConditionalClass.class)
@Order(before = ShouldNotBeTaken.class)
public class ShouldBeTaken implements ConditionalClass {
}
