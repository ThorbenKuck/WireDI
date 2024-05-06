package com.wiredi.runtime.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CompletionStageSubscription<T> {

    private boolean active = true;

    public CompletionStageSubscription(
            @NotNull CompletionStage<@NotNull T> source,
            @NotNull List<@NotNull BiConsumer<@Nullable T, @Nullable Throwable>> sinks
    ) {
        source.whenComplete((content, exception) -> {
            if (active) {
                sinks.forEach(it -> it.accept(content, exception));
            }
        });
    }

    public static <T> Builder<T> createFor(@NotNull CompletionStage<@NotNull T> source) {
        return new Builder<>(source);
    }

    @NotNull
    public CompletionStageSubscription<T> cancel() {
        this.active = false;
        return this;
    }

    public static class Builder<T> {
        private final @NotNull CompletionStage<@NotNull T> source;

        private final List<BiConsumer<T, Throwable>> sinks = new ArrayList<>();

        public Builder(@NotNull CompletionStage<@NotNull T> source) {
            this.source = source;
        }

        @NotNull
        public Builder<T> onCompletion(@NotNull BiConsumer<T, Throwable> sink) {
            this.sinks.add(sink);
            return this;
        }

        @NotNull
        public Builder<T> onCompletion(@NotNull Consumer<T> sink) {
            return onCompletion((t, throwable) -> sink.accept(t));
        }

        @NotNull
        public Builder<T> onCompletion(@NotNull Runnable sink) {
            return onCompletion((t, throwable) -> sink.run());
        }

        public CompletionStageSubscription<T> build() {
            return new CompletionStageSubscription<>(source, sinks);
        }
    }
}
