package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.errors.ExceptionHandler;
import com.wiredi.runtime.domain.errors.results.ExceptionHandlingResult;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A wrapper for maintaining strategies how exceptions should be handled.
 * <p>
 * Whenever an exception arises, this class can be consulted to determine an instance of the {@link ExceptionHandler}
 * to handle the exception.
 * <p>
 * The ExceptionHandler is connected to the {@link WireContainer},
 * where it can be accessed by {@link WireContainer#exceptionHandler()}.
 * You can provide {@link ExceptionHandler} instances either manually by calling
 * {@link WireContainer#exceptionHandler()},
 * followed by {@link ExceptionHandlerContext#register(Class, ExceptionHandler)},
 * or implicitly by declaring an {@link ExceptionHandler}
 * exposed as a bean by annotating it with {@link com.wiredi.annotations.Wire}.
 * <p>
 * To reduce computational complexity, the ExceptionHandler caches found instances of the {@link ExceptionHandler}.
 *
 * @see WireContainer
 * @see ExceptionHandler
 */
public class ExceptionHandlerContext {

    private static final Logging logger = Logging.getInstance(ExceptionHandlerContext.class);
    private WireContainer wireRepository;
    private final Map<Class<? extends Throwable>, Collection<ExceptionHandler<? extends Throwable>>> cache = new HashMap<>();

    public ExceptionHandlerContext(WireContainer wireRepository) {
        this.wireRepository = wireRepository;
    }

    /**
     * Sets the WireContext for this ExceptionHandlerContext.
     * This method is used to resolve circular dependencies during initialization.
     *
     * @param wireRepository the WireContext to set
     */
    public void setWireContext(WireContainer wireRepository) {
        this.wireRepository = wireRepository;
    }

    /**
     * Handle a received Throwable.
     * <p>
     * This method will invoke {@link #getHandler(Throwable)} to determine handlers.
     * Once handled, this method will apply the {@link ExceptionHandlingResult}, which might throw an exception.
     *
     * @param throwable The throwable to handle
     * @throws Throwable If any {@link ExceptionHandler} throws an exception
     * @see ExceptionHandler
     * @see ExceptionHandlingResult
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // We ignore generics right here
    public void handle(@NotNull final Throwable throwable) throws Throwable {
        logger.debug(() -> "Handling Exception " + throwable);
        Collection<ExceptionHandler<? extends Throwable>> errorHandlers;
        try {
            errorHandlers = getHandler(throwable);
        } catch (Throwable t) {
            errorHandlers = Collections.emptyList();
        }
        if (errorHandlers.isEmpty()) {
            logger.debug(() -> "No error handler found for " + throwable + ". Rethrowing exception.");
            throw throwable;
        }

        for (ExceptionHandler exceptionHandler : errorHandlers) {
            var handlingResult = exceptionHandler.handle(throwable);
            if (handlingResult.apply()) {
                return;
            }
        }
    }

    /**
     * Manually register an {@link ExceptionHandler}.
     * <p>
     * This will append the provided {@code handler} to the list of error handlers matching the {@code errorType}.
     * It does not respect if the error handler already is registered.
     * In this case, it will be registered, and therefore invoked twice.
     *
     * @param errorType The type of error that is handled by the {@link ExceptionHandler}
     * @param handler The {@link ExceptionHandler} to handle an exception of {@code errorType}
     * @param <T> The generic type for the exception that is handled
     * @return This instance for fluent api access
     */
    public <T extends Throwable> ExceptionHandlerContext register(Class<T> errorType, ExceptionHandler<T> handler) {
        Collection<ExceptionHandler<? extends Throwable>> errorHandlers = cache.computeIfAbsent(errorType, k -> new ArrayList<>());
        errorHandlers.add(handler);
        return this;
    }

    private <T extends Throwable> Collection<ExceptionHandler<? extends Throwable>> getHandler(@NotNull final T throwable) {
        return cache.computeIfAbsent(throwable.getClass(), k ->
                wireRepository.getAll(
                        TypeIdentifier.of(ExceptionHandler.class)
                                .withGeneric(k)
                )
        );
    }
}
