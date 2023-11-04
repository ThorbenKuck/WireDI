package com.wiredi.processor.tck.domain.generics;

import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Wire;

@Wire
@Primary
public class PrimaryStringImpl implements GenericBase<String> {
}
