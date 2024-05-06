package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.errors.ErrorHandler;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ExceptionHandler {

    private final WireRepository wireRepository;
    private final Map<Class<?>, List<ErrorHandler<? extends Throwable>>> cache = new HashMap<>();
    private static final Logging logger = Logging.getInstance(ExceptionHandler.class);

    public ExceptionHandler(WireRepository wireRepository) {
        this.wireRepository = wireRepository;
    }

    public <T extends Throwable> void handleError(@NotNull final T throwable) throws Throwable {
        logger.debug(() -> "Handling Exception " + throwable);
        Collection<ErrorHandler<? extends Throwable>> errorHandlers = getHandler(throwable);
        if (errorHandlers.isEmpty()) {
            logger.debug(() -> "No error handler found for " + throwable + ". Rethrowing exception.");
            throw throwable;
        }
        for (ErrorHandler<? extends Throwable> errorHandler : errorHandlers) {
            var handlingResult = ((ErrorHandler<T>) errorHandler).handle(throwable);
            if (handlingResult.apply()) {
                return;
            }
        }
    }

    private <T extends Throwable> Collection<ErrorHandler<? extends Throwable>> getHandler(@NotNull final T throwable) {
        return cache.computeIfAbsent(throwable.getClass(), k ->
                wireRepository.getAll(
                        TypeIdentifier.of(ErrorHandler.class)
                                .withGeneric(k)
                )
        );
    }
}
