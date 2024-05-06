package com.wiredi.tests.async;

public class AsyncResultValue<T> extends AsyncResult<T> {

    public void set(T value) {
        noteInvocation();
        this.value = value;
    }
}
