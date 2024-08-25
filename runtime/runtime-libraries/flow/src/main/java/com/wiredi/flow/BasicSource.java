package com.wiredi.flow;

public class BasicSource<T> extends AbstractSource<T> {
    public void publish(T element) {
        publishNewInput(element);
    }
}
