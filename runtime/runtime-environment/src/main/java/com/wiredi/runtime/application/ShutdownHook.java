package com.wiredi.runtime.application;

public final class ShutdownHook extends Thread {
    public ShutdownHook(Runnable runnable) {
        super(runnable);
        setName("Wired Shutdown Hook");
        setPriority(Thread.NORM_PRIORITY + 2);
        setDaemon(false);
    }
}