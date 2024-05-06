package com.wiredi.processor.tck.domain;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;

@Wire
@ConditionalOnProperty(key = "foo.bar")
public class Dependency {
}
