package com.wiredi.health;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CompositeHealth extends AbstractHealth {

    @NotNull
    private final Map<@NotNull String, @NotNull Health> modules = new HashMap<>();

    public CompositeHealth() {
    }

    public CompositeHealth(@NotNull HealthStatus status) {
        super(status);
    }

    public CompositeHealth(@NotNull HealthStatus status, @NotNull Map<String, String> details) {
        super(status, details);
    }

    @Nullable
    public Health addModuleHealth(@NotNull String name, @NotNull Health health) {
        return modules.put(name, health);
    }

    @Nullable
    public Health addModuleHealth(@NotNull NamedHealth namedHealth) {
        return modules.put(namedHealth.name(), namedHealth.health());
    }

    @Nullable
    public Health getModuleHealth(@NotNull String name) {
        return modules.get(name);
    }
}
