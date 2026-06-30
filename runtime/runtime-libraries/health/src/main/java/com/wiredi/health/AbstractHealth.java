package com.wiredi.health;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractHealth implements ModifiableHealth {

    protected volatile HealthStatus status;
    protected final Map<String, String> details;

    protected AbstractHealth() {
        this.details = Collections.emptyMap();
        this.status = HealthStatus.CREATED;
    }

    protected AbstractHealth(HealthStatus status) {
        this.details = Collections.emptyMap();
        this.status = status;
    }

    protected AbstractHealth(Map<String, String> details) {
        this.details = Collections.unmodifiableMap(details);
        this.status = HealthStatus.CREATED;
    }

    protected AbstractHealth(HealthStatus status, Map<String, String> details) {
        this.details = Collections.unmodifiableMap(details);
        this.status = status;
    }

    public Map<String, String> details() {
        return details;
    }

    @Override
    public void setStatus(@NotNull HealthStatus status) {
        this.status = status;
    }

    @Override
    public @NotNull HealthStatus status() {
        return status;
    }
}
