package com.wiredi.tests;

import org.jetbrains.annotations.NotNull;

public record ApplicationIdentifier(
        @NotNull TestPropertiesInstance properties
) {
}
