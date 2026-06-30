package com.wiredi.health;

import org.jetbrains.annotations.NotNull;

public record HealthStatus(@NotNull String code) {

    /* Regular lifecycle states */
    /**
     * The component has been created but has not yet been started.
     */
    public static final HealthStatus CREATED = new HealthStatus("created");
    /**
     * The component has been started and is ready to accept requests.
     */
    public static final HealthStatus STARTING = new HealthStatus("starting");
    public static final HealthStatus UP = new HealthStatus("up");
    public static final HealthStatus STOPPING = new HealthStatus("stopping");
    public static final HealthStatus DOWN = new HealthStatus("down");

    /* Special lifecycle states. */
    public static final HealthStatus FAULTY = new HealthStatus("faulty");
    public static final HealthStatus DECOMMISSIONED = new HealthStatus("decommissioned");

}
