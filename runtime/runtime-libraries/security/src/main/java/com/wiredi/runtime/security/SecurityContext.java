package com.wiredi.runtime.security;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import com.wiredi.runtime.security.authentication.Authentication;
import com.wiredi.runtime.security.authentication.AuthenticationExtractor;
import com.wiredi.runtime.security.exceptions.UnauthenticatedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages security context and authentication state for the current thread.
 * Provides methods for handling authentication states and executing code with specific authentication contexts.
 */
public class SecurityContext {

    private static final ThreadLocal<SecurityState> state = new ThreadLocal<>();
    private final AuthenticationExtractor authenticationExtractor;

    public SecurityContext(AuthenticationExtractor authenticationExtractor) {
        this.authenticationExtractor = authenticationExtractor;
    }

    /**
     * Returns the current authentication from the thread-local security state.
     *
     * @return the current Authentication object, or null if no authentication is present
     */
    @Nullable
    public static Authentication currentAuthentication() {
        @Nullable SecurityState state = current();
        if (state == null) {
            return null;
        }

        return state.getAuthentication();
    }

    /**
     * Returns the current security state from the thread-local storage.
     *
     * @return the current SecurityState object, or null if no state is set
     */
    @Nullable
    public static SecurityState current() {
        return state.get();
    }

    /**
     * Retrieves the authentication from the current security state.
     *
     * @return the current Authentication object, or null if no state or authentication exists
     */
    @Nullable
    public Authentication getAuthentication() {
        @Nullable SecurityState state = getState();
        if (state == null) {
            return null;
        }

        return state.getAuthentication();
    }

    /**
     * Sets the authentication in the current security state.
     * Creates a new state if none exists.
     *
     * @param authentication the Authentication to set, may be null to clear authentication
     */
    public void setAuthentication(@Nullable Authentication authentication) {
        getOrSetState().setAuthentication(authentication);
    }

    /**
     * Executes the given runnable with authentication extracted from the provided source.
     * Clears authentication after execution.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute
     * @param <E> type of throwable that might be thrown
     * @throws E if the operation throws an exception
     * @throws UnauthenticatedException if authentication cannot be extracted from the source
     */
    public <E extends Throwable> void runAuthenticated(
            @NotNull Object authenticationSource,
            @NotNull ThrowingRunnable<E> authenticationSupplier
    ) throws E {
        setupAuthenticationOf(authenticationSource);

        try {
            authenticationSupplier.run();
        } finally {
            clearAuthentication();
        }
    }

    /**
     * Executes the given consumer with authentication extracted from the provided source.
     * Clears authentication after execution.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute with the authentication
     * @param <E> type of throwable that might be thrown
     * @throws E if the operation throws an exception
     * @throws UnauthenticatedException if authentication cannot be extracted from the source
     */
    public <E extends Throwable> void runAuthenticated(
            @NotNull Object authenticationSource,
            @NotNull ThrowingConsumer<Authentication, E> authenticationSupplier
    ) throws E {
        @NotNull Authentication authentication = setupAuthenticationOf(authenticationSource);

        try {
            authenticationSupplier.accept(authentication);
        } finally {
            clearAuthentication();
        }
    }

    /**
     * Executes the given supplier with authentication extracted from the provided source.
     * Clears authentication after execution.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute
     * @param <R> type of the result
     * @param <E> type of throwable that might be thrown
     * @return the result of the supplier
     * @throws E if the operation throws an exception
     * @throws UnauthenticatedException if authentication cannot be extracted from the source
     */
    public <R, E extends Throwable> R runAuthenticated(
            @NotNull Object authenticationSource,
            @NotNull ThrowingSupplier<R, E> authenticationSupplier
    ) throws E {
        setupAuthenticationOf(authenticationSource);

        try {
            return authenticationSupplier.get();
        } finally {
            clearAuthentication();
        }
    }

    /**
     * Executes the given function with authentication extracted from the provided source.
     * Clears authentication after execution.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute with the authentication
     * @param <R> type of the result
     * @param <E> type of throwable that might be thrown
     * @return the result of the function
     * @throws E if the operation throws an exception
     * @throws UnauthenticatedException if authentication cannot be extracted from the source
     */
    public <R, E extends Throwable> R runAuthenticated(
            @NotNull Object authenticationSource,
            @NotNull ThrowingFunction<Authentication, R, E> authenticationSupplier
    ) throws E {
        @NotNull Authentication authentication = setupAuthenticationOf(authenticationSource);

        try {
            return authenticationSupplier.apply(authentication);
        } finally {
            clearAuthentication();
        }
    }

    /**
     * Executes the given runnable with authentication from the source while preserving previous authentication.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute
     * @param <E> type of throwable that might be thrown
     * @throws E if the operation throws an exception
     */
    public <E extends Throwable> void run(
            @NotNull Object authenticationSource,
            @NotNull ThrowingRunnable<E> authenticationSupplier
    ) throws E {
        Authentication previousAuthentication = getAuthentication();
        Authentication authentication = getAuthenticationOf(authenticationSource);
        if (authentication != null) {
            setAuthentication(authentication);
        }

        try {
            authenticationSupplier.run();
        } finally {
            if (previousAuthentication != null) {
                setAuthentication(previousAuthentication);
            } else {
                clearAuthentication();
            }
        }
    }

    /**
     * Executes the given consumer with authentication from the source while preserving previous authentication.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute with the authentication
     * @param <E> type of throwable that might be thrown
     * @throws E if the operation throws an exception
     */
    public <E extends Throwable> void run(
            @NotNull Object authenticationSource,
            @NotNull ThrowingConsumer<Authentication, E> authenticationSupplier
    ) throws E {
        Authentication previousAuthentication = getAuthentication();
        Authentication authentication = getAuthenticationOf(authenticationSource);
        if (authentication != null) {
            setAuthentication(authentication);
        }

        try {
            authenticationSupplier.accept(authentication);
        } finally {
            if (previousAuthentication != null) {
                setAuthentication(previousAuthentication);
            } else {
                clearAuthentication();
            }
        }
    }

    /**
     * Executes the given supplier with authentication from the source while preserving previous authentication.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute
     * @param <R> type of the result
     * @param <E> type of throwable that might be thrown
     * @return the result of the supplier
     * @throws E if the operation throws an exception
     */
    public <R, E extends Throwable> R run(
            @NotNull Object authenticationSource,
            @NotNull ThrowingSupplier<R, E> authenticationSupplier
    ) throws E {
        Authentication previousAuthentication = getAuthentication();
        Authentication authentication = getAuthenticationOf(authenticationSource);
        if (authentication != null) {
            setAuthentication(authentication);
        }

        try {
            return authenticationSupplier.get();
        } finally {
            if (previousAuthentication != null) {
                setAuthentication(previousAuthentication);
            } else {
                clearAuthentication();
            }
        }
    }

    /**
     * Executes the given function with authentication from the source while preserving previous authentication.
     *
     * @param authenticationSource source object to extract authentication from
     * @param authenticationSupplier operation to execute with the authentication
     * @param <R> type of the result
     * @param <E> type of throwable that might be thrown
     * @return the result of the function
     * @throws E if the operation throws an exception
     */
    public <R, E extends Throwable> R run(
            @NotNull Object authenticationSource,
            @NotNull ThrowingFunction<Authentication, R, E> authenticationSupplier
    ) throws E {
        Authentication previousAuthentication = getAuthentication();
        Authentication authentication = getAuthenticationOf(authenticationSource);
        if (authentication != null) {
            setAuthentication(authentication);
        }

        try {
            return authenticationSupplier.apply(authentication);
        } finally {
            if (previousAuthentication != null) {
                setAuthentication(previousAuthentication);
            } else {
                clearAuthentication();
            }
        }
    }

    /**
     * Sets up authentication from the given source and returns it.
     *
     * @param source source object to extract authentication from
     * @return the extracted Authentication
     * @throws UnauthenticatedException if no authentication can be extracted from the source
     */
    @NotNull
    public Authentication setupAuthenticationOf(@NotNull Object source) {
        @Nullable Authentication authentication = getAuthenticationOf(source);
        if (authentication == null) {
            throw new UnauthenticatedException();
        }

        getOrSetState().setAuthentication(authentication);
        return authentication;
    }

    /**
     * Extracts authentication information from the provided source object.
     *
     * @param source source object to extract authentication from
     * @return the extracted Authentication, or null if none could be extracted
     */
    @Nullable
    public Authentication getAuthenticationOf(@NotNull Object source) {
        return authenticationExtractor.getAuthentication(source);
    }

    /**
     * Removes the security state from the thread-local storage.
     */
    public void clearAuthentication() {
        state.remove();
    }

    @Nullable
    private SecurityState getState() {
        return state.get();
    }

    @NotNull
    private SecurityState getOrSetState() {
        @Nullable SecurityState state = SecurityContext.state.get();
        if (state != null) {
            return state;
        }

        @NotNull SecurityState newState = new SecurityState();
        SecurityContext.state.set(newState);
        return newState;
    }
}