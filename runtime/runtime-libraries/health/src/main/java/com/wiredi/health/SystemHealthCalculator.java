package com.wiredi.health;

import java.util.Collection;

public interface SystemHealthCalculator {

    SystemHealthCalculator FIRST_FAULT = components -> {
        for (Health component : components) {
            if (component.status() == HealthStatus.FAULTY) {
                return HealthStatus.FAULTY;
            }
        }
        return HealthStatus.UP;
    };

    HealthStatus calculateHealth(Collection<Health> components);
}
