package com.wiredi.processor.plugins;

import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.*;
import org.jetbrains.annotations.NotNull;

public interface CompilerEntityPlugin extends Plugin {

    default void handle(@NotNull AspectAwareProxyEntity proxy) {
    }

    default void handle(@NotNull IdentifiableProviderEntity entity) {
    }

    default void handle(@NotNull AspectHandlerEntity entity) {
    }

    default void handle(@NotNull EnvironmentConfigurationEntity entity) {
    }

    default void handle(@NotNull WireBridgeEntity entity) {
    }

    default void handleUnsupported(@NotNull ClassEntity entity) {

    }
}
