package com.wiredi.compiler.processor.lang.concurrent;

import java.util.Collections;
import java.util.List;

public class EmptyThreadBarrier implements ThreadBarrier {

    public static final EmptyThreadBarrier INSTANCE = new EmptyThreadBarrier();

    @Override
    public int getInitialSemaphoreCount() {
        return 0;
    }

    @Override
    public int countNotFinishedThreads() {
        return 0;
    }

    @Override
    public List<ContextRunnable> getRunnableList() {
        return Collections.emptyList();
    }

    @Override
    public void run() {
        // Do nothing
    }
}
