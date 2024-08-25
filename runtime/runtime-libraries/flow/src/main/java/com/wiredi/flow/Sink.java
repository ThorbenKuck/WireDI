package com.wiredi.flow;

public interface Sink<T> {

    boolean accept(T t);

}
