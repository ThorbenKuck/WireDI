package com.wiredi.processor.tck.domain.cyclic;

import com.wiredi.annotations.Wire;

@Wire
public class D {
    private final A a;

    public D(A a) {
        this.a = a;
    }
}
