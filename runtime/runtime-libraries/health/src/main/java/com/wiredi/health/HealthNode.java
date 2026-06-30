package com.wiredi.health;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HealthNode extends AbstractHealth {

    public HealthNode() {
        this.status = HealthStatus.CREATED;
    }

    public HealthNode(@NotNull HealthStatus status) {
        this.status = status;
    }

    public HealthNode(@NotNull HealthStatus status, @NotNull Map<String, String> details) {
        super(details);
        this.status = status;
    }
}
