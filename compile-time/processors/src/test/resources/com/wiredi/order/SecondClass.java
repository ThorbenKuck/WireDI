package com.wiredi.order.input;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Wire;

@Order(before = com.wiredi.order.input.FirstClass.class)
@Wire
public class SecondClass {
}
