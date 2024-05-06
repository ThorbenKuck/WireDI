package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;

@Wire
public class TransactionState {

    private boolean active;

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
