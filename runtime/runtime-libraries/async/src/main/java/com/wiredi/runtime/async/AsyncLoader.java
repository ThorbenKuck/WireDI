package com.wiredi.runtime.async;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AsyncLoader {

    private static final Logging logger = Logging.getInstance(AsyncLoader.class);
    private static final String THREAD_KEY = "wiredi.eager-loading.max-threads";
    private static final String IS_VIRTUAL_KEY = "wiredi.eager-loading.virtual-threads";
    private static final int threads;
    private static final ExecutorService executorService;

    static {
        String virtual = System.getenv().getOrDefault(IS_VIRTUAL_KEY, System.getProperty(IS_VIRTUAL_KEY, "true"));
        if (Boolean.parseBoolean(virtual)) {
            logger.debug("AsyncLoader is configured using VirtualThreads");
            threads = -1;
            executorService = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            String raw = System.getenv().getOrDefault(THREAD_KEY, System.getProperty(THREAD_KEY));
            if (raw == null) {
                threads = Runtime.getRuntime().availableProcessors();
            } else {
                try {
                    threads = Integer.parseInt(raw);
                } catch (NumberFormatException t) {
                    logger.error(() -> "Invalid value for property " + THREAD_KEY + ": Should be an INT");
                    logger.error("Unexpected exception", t);
                    throw new IllegalArgumentException("The property " + THREAD_KEY + " must be a number!", t);
                }
            }

            logger.debug("AsyncLoader is configured using " + threads + " PlatformThreads");
            executorService = Executors.newFixedThreadPool(threads, new DaemonThreadFactory());
        }
    }

    public static <T> void load(ThrowingSupplier<T, ?> supplier, Consumer<T> resultConsumer) {
        executorService.execute(() -> {
            try {
                resultConsumer.accept(supplier.get());
            } catch (Throwable e) {
                if (e instanceof RuntimeException r) {
                    throw r;
                }
                throw new UndeclaredThrowableException(e);
            }
        });
    }

    public static <T, E extends Throwable> void load(ThrowingSupplier<T, E> supplier, Consumer<T> resultConsumer, Consumer<Throwable> exceptionHandler) {
        executorService.execute(() -> {
            try {
                resultConsumer.accept(supplier.get());
            } catch (Throwable e) {
                exceptionHandler.accept(e);
            }
        });
    }

    public static <T> CompletionStage<T> load(@NotNull final ThrowingSupplier<T, ?> supplier) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        load(supplier, completableFuture::complete);
        return completableFuture;
    }

    public static CompletionStage<Void> run(@NotNull final Runnable runnable) {
        CompletableFuture<Void> completionStage = new CompletableFuture<>();
        executorService.execute(() -> {
            try {
                runnable.run();
                completionStage.complete(null);
            } catch (Throwable t) {
                completionStage.completeExceptionally(t);
            }
        });
        return completionStage;
    }
}
