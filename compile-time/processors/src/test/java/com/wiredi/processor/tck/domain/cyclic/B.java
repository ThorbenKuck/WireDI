package com.wiredi.processor.tck.domain.cyclic;

import com.wiredi.annotations.Wire;

@Wire
public class B {

    private final C c;
    private final E e;

    public B(C c, E e) {
        this.c = c;
        this.e = e;
    }
}
