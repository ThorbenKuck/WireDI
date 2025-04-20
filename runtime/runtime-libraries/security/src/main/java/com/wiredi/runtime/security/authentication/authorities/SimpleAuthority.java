package com.wiredi.runtime.security.authentication.authorities;

public class SimpleAuthority implements Authority {

    private final String name;

    public SimpleAuthority(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Authority authority) {
        if (authority instanceof SimpleAuthority simpleAuthority) {
            return simpleAuthority.name.equals(name);
        }

        return false;
    }
}
