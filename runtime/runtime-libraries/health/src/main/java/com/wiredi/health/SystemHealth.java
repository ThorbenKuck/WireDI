package com.wiredi.health;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SystemHealth implements ModifiableHealth {

    @NotNull
    protected volatile HealthStatus status = HealthStatus.CREATED;
    @NotNull
    private final Map<@NotNull String, @NotNull Health> components = new HashMap<>();
    @NotNull
    private SystemHealthCalculator calculator = SystemHealthCalculator.FIRST_FAULT;

    @Nullable
    public Health addComponent(@NotNull String name, @NotNull Health health) {
        return components.put(name, health);
    }

    @Nullable
    public Health addComponent(@NotNull NamedHealth namedHealth) {
        return addComponent(namedHealth.name(), namedHealth.health());
    }

    @Override
    public void setStatus(@NotNull HealthStatus status) {
        this.status = status;
    }

    @Override
    public @NotNull HealthStatus status() {
        return status;
    }

    public void setHealthCalculator(@NotNull SystemHealthCalculator calculator) {
        this.calculator = calculator;
    }

    public HealthStatus calculateStatus() {
        HealthStatus healthStatus = calculator.calculateHealth(components.values());
        this.status = healthStatus;
        return healthStatus;

    }
}
