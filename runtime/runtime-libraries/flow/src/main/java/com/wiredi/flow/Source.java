package com.wiredi.flow;

import java.util.function.Consumer;

public interface Source<T> {

    int consumerSize();

    void addConsumer(Consumer<T> consumer);

    void addSink(Sink<T> sink);

}
