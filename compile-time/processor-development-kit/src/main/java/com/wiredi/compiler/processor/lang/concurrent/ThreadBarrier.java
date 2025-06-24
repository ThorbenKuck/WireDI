package com.wiredi.compiler.processor.lang.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface ThreadBarrier {
    int getInitialSemaphoreCount();

    int countNotFinishedThreads();

    List<ContextRunnable> getRunnableList();

    void run();

    class Builder {

        private final List<ContextRunnable> runnableList = new ArrayList<>();
        private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        public Builder withExecutorService(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Builder withRunnable(ContextRunnable first) {
            return withRunnables(first);
        }

        public Builder withRunnables(ContextRunnable first, ContextRunnable... other) {
            this.runnableList.add(first);
            this.runnableList.addAll(Arrays.asList(other));
            return this;
        }

        public Builder withRunnables(Collection<ContextRunnable> collection) {
            this.runnableList.addAll(collection);
            return this;
        }

        public void run() {
            ThreadBarrier build = build();
            build.run();
        }

        public ThreadBarrier build() {
            if (runnableList.isEmpty() || executorService == null) {
                return EmptyThreadBarrier.INSTANCE;
            }
            return new ExecutorServiceThreadBarrier(executorService, runnableList);
        }
    }

}
