package com.wiredi.runtime.async.fences;

import com.wiredi.logging.Logging;

public class SimpleFence implements Fence {

    private final Runnable runnable;
    private static final Logging logger = Logging.getInstance(SimpleFence.class);

    public SimpleFence(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void pass() {
        logger.trace(() -> "Fence passed");
        runnable.run();
    }
}
