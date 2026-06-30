package com.wiredi.health;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public interface Health {

    @NotNull
    HealthStatus status();

    @NotNull
    default NamedHealth named(@NotNull String name) {
        return new NamedHealth(name, this);
    }

    // Builder API
    static @NotNull HealthNode builder(Consumer<NodeBuilder> customizer) {
        NodeBuilder b = builder();
        customizer.accept(b);
        return b.build();
    }
    static @NotNull NodeBuilder builder() {
        return new NodeBuilder.DefaultNodeBuilder();
    }

    static @NotNull CompositeHealth compositeBuilder(Consumer<CompositeBuilder> customizer) {
        CompositeBuilder b = compositeBuilder();
        customizer.accept(b);
        return b.build();
    }

    static @NotNull CompositeBuilder compositeBuilder() {
        return new CompositeBuilder.DefaultCompositeBuilder();
    }

    interface AggregatedBuilder {
        @NotNull NodeBuilder node();
        @NotNull CompositeBuilder composite();
        @NotNull Health build();

        default @NotNull Health buildNode(@NotNull Consumer<NodeBuilder> customizer) {
            NodeBuilder b = node();
            customizer.accept(b);
            return b.build();
        }

        default @NotNull Health buildComposite(@NotNull Consumer<CompositeBuilder> customizer) {
            CompositeBuilder b = composite();
            customizer.accept(b);
            return b.build();
        }

        class DefaultAggregatedBuilder implements AggregatedBuilder {
            private NodeBuilder nodeBuilder;
            private CompositeBuilder compositeBuilder;

            @Override
            public @NotNull NodeBuilder node() {
                this.nodeBuilder = new NodeBuilder.DefaultNodeBuilder();
                this.compositeBuilder = null;
                return this.nodeBuilder;
            }

            @Override
            public @NotNull CompositeBuilder composite() {
                this.compositeBuilder = new CompositeBuilder.DefaultCompositeBuilder();
                this.nodeBuilder = null;
                return this.compositeBuilder;
            }

            @Override
            public @NotNull Health build() {
                if (compositeBuilder != null) {
                    return compositeBuilder.build();
                }
                if (nodeBuilder != null) {
                    return nodeBuilder.build();
                }
                // Default to an empty node
                return new HealthNode();
            }
        }
    }

    interface NodeBuilder {
        @NotNull NodeBuilder status(@NotNull HealthStatus status);
        @NotNull NodeBuilder detail(@NotNull String key, @NotNull String value);
        @NotNull NodeBuilder details(@NotNull Map<String, String> details);
        @NotNull HealthNode build();

        class DefaultNodeBuilder implements NodeBuilder {
            private HealthStatus status = HealthStatus.CREATED;
            private final Map<String, String> details = new HashMap<>();

            @Override
            public @NotNull NodeBuilder status(@NotNull HealthStatus status) {
                this.status = Objects.requireNonNull(status, "status");
                return this;
            }

            @Override
            public @NotNull NodeBuilder detail(@NotNull String key, @NotNull String value) {
                details.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
                return this;
            }

            @Override
            public @NotNull NodeBuilder details(@NotNull Map<String, String> details) {
                this.details.putAll(Objects.requireNonNull(details, "details"));
                return this;
            }

            @Override
            public @NotNull HealthNode build() {
                return new HealthNode(status, details);
            }
        }
    }

    interface CompositeBuilder {
        @NotNull CompositeBuilder status(@NotNull HealthStatus status);
        @NotNull CompositeBuilder detail(@NotNull String key, @NotNull String value);
        @NotNull CompositeBuilder details(@NotNull Map<String, String> details);
        @NotNull CompositeBuilder module(@NotNull String name, @NotNull Consumer<AggregatedBuilder> moduleBuilder);
        @NotNull CompositeHealth build();

        class DefaultCompositeBuilder implements CompositeBuilder {
            private HealthStatus status = HealthStatus.CREATED;
            private final Map<String, String> details = new HashMap<>();
            private final Map<String, Health> modules = new HashMap<>();

            @Override
            public @NotNull CompositeBuilder status(@NotNull HealthStatus status) {
                this.status = Objects.requireNonNull(status, "status");
                return this;
            }

            @Override
            public @NotNull CompositeBuilder detail(@NotNull String key, @NotNull String value) {
                details.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
                return this;
            }

            @Override
            public @NotNull CompositeBuilder details(@NotNull Map<String, String> details) {
                this.details.putAll(Objects.requireNonNull(details, "details"));
                return this;
            }

            @Override
            public @NotNull CompositeBuilder module(@NotNull String name, @NotNull Consumer<AggregatedBuilder> moduleBuilder) {
                Objects.requireNonNull(name, "name");
                Objects.requireNonNull(moduleBuilder, "moduleBuilder");
                AggregatedBuilder child = new AggregatedBuilder.DefaultAggregatedBuilder();
                moduleBuilder.accept(child);
                Health built = child.build();
                modules.put(name, built);
                return this;
            }

            @Override
            public @NotNull CompositeHealth build() {
                CompositeHealth composite = new CompositeHealth(status, details);
                for (Map.Entry<String, Health> entry : modules.entrySet()) {
                    composite.addModuleHealth(entry.getKey(), entry.getValue());
                }
                return composite;
            }
        }
    }
}
