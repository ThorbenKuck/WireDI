package com.wiredi.health;

import org.jetbrains.annotations.NotNull;

public record NamedHealth(
        @NotNull String name,
        @NotNull Health health
) {
}