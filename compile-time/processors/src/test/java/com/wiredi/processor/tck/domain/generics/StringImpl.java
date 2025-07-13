package com.wiredi.processor.tck.domain.generics;

import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Wire;

@Wire
@Primary
public class StringImpl implements GenericBase<String> {
}
