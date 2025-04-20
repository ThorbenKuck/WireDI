package com.wiredi.runtime.security.infrastructure;

import com.wiredi.runtime.security.authentication.Authentication;
import com.wiredi.runtime.security.authentication.AuthenticationProvider;
import com.wiredi.runtime.security.authentication.UsernamePasswordAuthentication;
import org.jetbrains.annotations.Nullable;

public class ConsumerRecordAuthenticationProvider implements AuthenticationProvider {
    @Override
    public @Nullable Authentication extract(Object source) {
        if (!(source instanceof ConsumerRecord record)) {
            return null;
        }
        String username = record.getHeader("username");
        String password = record.getHeader("password");

        if (username != null && password != null) {
            return new UsernamePasswordAuthentication(username, password)
                    .setAuthenticated(username.equals("user") && password.equals("pass"));
        }

        return null;
    }
}
