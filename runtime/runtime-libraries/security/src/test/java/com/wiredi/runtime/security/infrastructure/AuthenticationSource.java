package com.wiredi.runtime.security.infrastructure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AuthenticationSource {

    private final Map<String, String> headers = new HashMap<>();

    public void setHeader(@NotNull String key, @NotNull String value) {
        this.headers.put(key, value);
    }

    @Nullable
    public String getHeader(String key) {
        return headers.get(key);
    }
}
