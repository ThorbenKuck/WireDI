package com.wiredi.processor.tck.domain.example;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;

@Wire
@Order(10)
public class V3Engine implements Engine {
}
