package com.wiredi.flow;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractSource<T> implements Source<T> {

    private final List<Consumer<T>> consumers = new ArrayList<>();

    @Override
    public int consumerSize() {
        return consumers.size();
    }

    @Override
    public void addConsumer(Consumer<T> flowElement) {
        this.consumers.add(flowElement);
    }

    @Override
    public void addSink(Sink<T> sink) {
        this.consumers.add(new SinkConsumer<>(sink));
    }

    protected void publishNewInput(T element) {
        consumers.forEach(consumer -> consumer.accept(element));
    }

    private static final class SinkConsumer<T> implements Consumer<T> {

        private final Sink<T> sink;

        private SinkConsumer(Sink<T> sink) {
            this.sink = sink;
        }

        @Override
        public void accept(T t) {
            sink.accept(t);
        }

        @NotNull
        @Override
        public Consumer<T> andThen(@NotNull Consumer<? super T> after) {
            throw new UnsupportedOperationException();
        }
    }
}
