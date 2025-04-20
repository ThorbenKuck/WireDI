package com.wiredi.runtime.security;

import com.wiredi.runtime.security.authentication.Authentication;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the security state for a thread in the application.
 * This class maintains the authentication information for the current execution context
 * and is typically managed by {@link SecurityContext}.
 *
 * <p>The security state is primarily thread-local and holds authentication details that can be
 * accessed and modified during the execution of security-aware operations. It serves
 * as a container for the current {@link Authentication} object.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SecurityState state = new SecurityState();
 *
 * // Set authentication
 * Authentication auth = createAuthentication();
 * state.setAuthentication(auth);
 *
 * // Get current authentication
 * Authentication current = state.getAuthentication();
 * }</pre>
 *
 * <p>This class is designed to work in conjunction with {@link SecurityContext} to provide
 * thread-safe access to authentication information. It's important to note that this class
 * itself is not thread-safe and should be managed properly through {@link SecurityContext}.</p>
 *
 * @see SecurityContext
 * @see Authentication
 */
public class SecurityState {

    /**
     * The current authentication object associated with this security state.
     * Can be null if no authentication is present.
     */
    @Nullable
    private Authentication authentication;

    /**
     * Retrieves the current authentication object associated with this security state.
     *
     * <p>The authentication object contains information about the currently authenticated
     * principal, including credentials and authorities. A null return value indicates
     * that no authentication is currently set in this security state.</p>
     *
     * @return the current {@link Authentication} object, or null if no authentication is set
     * @see Authentication
     */
    @Nullable
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Sets the authentication object for this security state.
     *
     * <p>This method updates the current authentication information. Setting a null value
     * effectively clears the authentication state. This operation is typically performed
     * by the {@link SecurityContext} during authentication operations or when clearing
     * the security context.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * SecurityState state = new SecurityState();
     *
     * // Set new authentication
     * state.setAuthentication(newAuth);
     *
     * // Clear authentication
     * state.setAuthentication(null);
     * }</pre>
     *
     * @param authentication the {@link Authentication} object to set, or null to clear authentication
     * @see SecurityContext
     */
    public void setAuthentication(@Nullable Authentication authentication) {
        this.authentication = authentication;
    }
}
