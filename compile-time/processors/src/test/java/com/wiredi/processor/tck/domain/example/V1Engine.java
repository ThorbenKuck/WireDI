package com.wiredi.processor.tck.domain.example;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Wire;

@Wire
@Order(0)
@Primary
public class V1Engine implements Engine {
}
