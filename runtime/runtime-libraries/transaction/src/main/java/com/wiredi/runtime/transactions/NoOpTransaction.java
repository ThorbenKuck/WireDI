package com.wiredi.runtime.transactions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoOpTransaction extends AbstractTransaction {
    public NoOpTransaction(@Nullable Transaction parent) {
        super(parent);
    }

    public NoOpTransaction() {
        super(null);
    }

    @Override
    protected boolean commit() {
        return false;
    }

    @Override
    protected boolean rollback() {
        return false;
    }

    @Override
    public boolean flushable() {
        return false;
    }

    @Override
    public @NotNull Transaction nest() {
        return new NoOpTransaction(this);
    }
}
