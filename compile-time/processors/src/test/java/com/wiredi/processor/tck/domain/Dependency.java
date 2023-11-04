package com.wiredi.processor.tck.domain;

import com.wiredi.annotations.Wire;
import com.wiredi.domain.conditional.builtin.ConditionalOnBean;
import com.wiredi.domain.conditional.builtin.ConditionalOnProperty;
import com.wiredi.processor.tck.ConditionalOnTrue;

@Wire
@ConditionalOnTrue
@ConditionalOnProperty(key = "foo.bar")
public class Dependency {
}
