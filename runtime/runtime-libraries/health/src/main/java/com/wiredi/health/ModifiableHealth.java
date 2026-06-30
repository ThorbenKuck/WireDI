package com.wiredi.health;

import org.jetbrains.annotations.NotNull;

public interface ModifiableHealth extends Health {

    void setStatus(@NotNull HealthStatus status);

}
