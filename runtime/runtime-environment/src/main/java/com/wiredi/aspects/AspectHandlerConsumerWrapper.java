package com.wiredi.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AspectHandlerConsumerWrapper implements AspectHandler {

    @NotNull
    private final Consumer<ExecutionContext> consumer;

    public AspectHandlerConsumerWrapper(@NotNull Consumer<ExecutionContext> consumer) {
        this.consumer = consumer;
    }

    @Override
    public @Nullable Object process(@NotNull ExecutionContext context) {
        consumer.accept(context);
        return null;
    }
}
