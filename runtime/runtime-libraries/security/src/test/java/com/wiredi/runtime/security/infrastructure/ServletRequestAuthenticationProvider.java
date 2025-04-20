package com.wiredi.runtime.security.infrastructure;

import com.wiredi.runtime.security.authentication.Authentication;
import com.wiredi.runtime.security.authentication.AuthenticationProvider;
import com.wiredi.runtime.security.authentication.UsernamePasswordAuthentication;
import org.jetbrains.annotations.Nullable;

public class ServletRequestAuthenticationProvider implements AuthenticationProvider {
    @Override
    public @Nullable Authentication extract(Object source) {
        if (!(source instanceof ServletRequest request)) {
            return null;
        }

        String username = request.getHeader("username");
        String password = request.getHeader("password");

        if (username != null && password != null) {
            return new UsernamePasswordAuthentication(username, password)
                    .setAuthenticated(username.equals("user") && password.equals("pass"));
        }

        return null;
    }

    @Override
    public boolean canExtractFrom(Object source) {
        return source instanceof ServletRequest;
    }
}
