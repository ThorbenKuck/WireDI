package com.wiredi.tests.instance;

import com.wiredi.tests.TestPropertiesInstance;
import org.jetbrains.annotations.NotNull;

public record InstanceIdentifier(
        @NotNull TestPropertiesInstance properties
) {
}
