package com.wiredi.tests.async;

import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AsyncResultList<T> extends AsyncResult<List<T>> {

    public AsyncResultList() {
        super(ArrayList::new);
    }

    public void set(Collection<T> value) {
        noteInvocations(value.size());
        this.value.clear();
        this.value.addAll(value);
    }

    public void add(T t) {
        if (isCompleted()) {
            errors.add(new AssertionFailedError("Tried to update an already completed AsyncResult " + this));
        } else {
            noteInvocation();
            this.value.add(t);
        }
    }
}
