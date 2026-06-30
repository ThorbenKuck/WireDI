package com.wiredi.processor.tck.domain.cyclic;

import com.wiredi.annotations.Wire;

@Wire
public class C {

    private final D d;

    public C(D d) {
        this.d = d;
    }
}
