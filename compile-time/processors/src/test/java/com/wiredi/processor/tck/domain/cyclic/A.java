package com.wiredi.processor.tck.domain.cyclic;

import com.wiredi.annotations.Wire;

@Wire
public class A {

    private final B b;

    public A(B b) {
        this.b = b;
    }
}
