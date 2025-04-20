package com.wiredi.runtime.security.authentication;

import com.wiredi.runtime.security.authentication.authorities.Authority;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

/**
 * This interface represents any kind of Authentication.
 * <p>
 * If present in code, this class represents that the process is authenticated.
 * The implementation determines the kind of authentication.
 * <p>
 * For example, if a user is authenticated using username/password, a {@link UsernamePasswordAuthentication} can be used.
 * <p>
 * Every authentication has a list of authorities associated with it.
 * The representation of authority depends on the authentication type.
 */
public interface Authentication extends Principal, Serializable {

    /**
     * All authorities associated with this Authentication
     * <p>
     * Depending on the implementation, the Authentication can be constructed, but contain an empty list of authorities.
     * Most notably, this can be the case of the authentication is incorrect.
     *
     * @return a list of all authorities.
     */
    @NotNull
    List<Authority> authorities();

    default boolean isAuthenticated() {
        return state() == Authentication.State.AUTHENTICATED;
    }

    default boolean isChecked() {
        return state() != State.UNCHECKED;
    }

    /**
     * This method returns the state of the authentication.
     * <p>
     * The state describes what has happened with this authentication and if it's valid.
     *
     * @return the state of the authentication.
     */
    @NotNull
    State state();

    /**
     * {@inheritDoc}
     */
    @Override
    default String getName() {
        return getClass().getSimpleName();
    }

    enum State {
        UNCHECKED,
        AUTHENTICATED,
        NOT_AUTHENTICATED;
    }
}
