package com.wiredi.runtime.security.authentication;

import com.wiredi.runtime.security.authentication.authorities.Authority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleAuthentication<T extends SimpleAuthentication<T>> implements Authentication {

    private final List<Authority> authorities = new ArrayList<>();
    private Authentication.State state = Authentication.State.UNCHECKED;

    @Override
    public Authentication.@NotNull State state() {
        return state;
    }

    public T setAuthenticated(boolean authenticated) {
        if (authenticated) {
            this.state = Authentication.State.AUTHENTICATED;
        } else {
            this.state = Authentication.State.NOT_AUTHENTICATED;
        }

        return (T) this;
    }

    @Override
    public @NotNull List<Authority> authorities() {
        return authorities;
    }
}
