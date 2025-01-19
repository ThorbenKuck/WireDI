package com.wiredi.processor.tck.domain.example;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;

@Wire
@Order(after = V1Engine.class, before = V3Engine.class)
public class V2Engine implements Engine {
}
