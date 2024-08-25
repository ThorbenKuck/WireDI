package com.wiredi.compiler.processor.plugins;

import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.TypeElement;

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

    default void handleUnsupported(@NotNull ClassEntity<?> entity) {
    }

    default void onFlush(@NotNull ClassEntity<?> entity) {
    }

    @Nullable
    default ClassEntity<? extends ClassEntity<? extends ClassEntity<?>>> implementInterface(@NotNull TypeElement typeElement, @Nullable Wire wire) {
        return null;
    }
}
