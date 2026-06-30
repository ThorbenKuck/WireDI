package com.wiredi.health;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthAggregatedBuilderTest {

    @Test
    void shouldBuildSimpleNode_withDefaults() {
        // Arrange
        // Act
        Health health = Health.builder().build();

        // Assert
        assertThat(health).isInstanceOf(HealthNode.class);
        assertThat(health.status()).isEqualTo(HealthStatus.CREATED);
        assertThat(((HealthNode) health).details()).isEmpty();
    }

    @Test
    void shouldBuildNode_withStatusAndDetails() {
        // Arrange
        // Act
        Health health = Health.builder()
                .status(HealthStatus.UP)
                .detail("k1", "v1")
                .details(Map.of("k2", "v2"))
                .build();

        // Assert
        assertThat(health).isInstanceOf(HealthNode.class);
        assertThat(health.status()).isEqualTo(HealthStatus.UP);
        assertThat(((HealthNode) health).details())
                .containsEntry("k1", "v1")
                .containsEntry("k2", "v2");
    }

    @Test
    void shouldBuildComposite_withModulesViaLambda() {
        // Arrange
        // Act
        CompositeHealth composite = Health.compositeBuilder()
                .status(HealthStatus.STARTING)
                .detail("env", "test")
                .module("db", b -> b.node().status(HealthStatus.UP).detail("version", "14").build())
                .module("cache", b -> b.node().status(HealthStatus.DOWN).detail("reason", "miss"))
                .build();

        // Assert
        assertThat(composite.status()).isEqualTo(HealthStatus.STARTING);
        assertThat(composite.details()).containsEntry("env", "test");
        // module collision behavior validated by getModuleHealth in CompositeHealthTest; here just ensure entries are present
        assertThat(composite.getModuleHealth("db")).isNotNull();
    }

    @Test
    void shouldBuildNestedComposite_threeLevels() {
        // Arrange
        // Act
        CompositeHealth root = Health.compositeBuilder()
                .status(HealthStatus.UP)
                .module("api", b -> b.node().status(HealthStatus.UP))
                .module("services", b -> b.composite()
                        .status(HealthStatus.STARTING)
                        .module("user", cb -> cb.node().status(HealthStatus.UP))
                        .module("order", cb -> cb.composite()
                                .status(HealthStatus.DOWN)
                                .module("db", x -> x.node().status(HealthStatus.DOWN))
                        )
                )
                .build();

        // Assert
        assertThat(root.status()).isEqualTo(HealthStatus.UP);

        // Verify nested modules exist by overwriting and checking previous
        Health prev1 = root.getModuleHealth("api");
        assertThat(prev1).isNotNull();
        Health services = root.getModuleHealth("services");
        assertThat(services).isInstanceOf(CompositeHealth.class);
    }

    @Test
    void shouldUseConvenienceBuildMethods_forNodeAndComposite() {
        // Arrange
        // Act
        HealthNode node = Health.builder(n -> n.status(HealthStatus.STOPPING).detail("k", "v"));
        Health composite = Health.compositeBuilder(c -> c
                .status(HealthStatus.FAULTY)
                .module("x", b -> b.node().status(HealthStatus.DOWN))
        );

        // Assert
        assertThat(node).isInstanceOf(HealthNode.class);
        assertThat(node.status()).isEqualTo(HealthStatus.STOPPING);
        assertThat(node.details()).containsEntry("k", "v");
        assertThat(composite).isInstanceOf(CompositeHealth.class);
        assertThat(composite.status()).isEqualTo(HealthStatus.FAULTY);
    }

    @Test
    void shouldPreferLastModuleDefinition_whenSameNameIsUsedMultipleTimes() {
        // Arrange
        CompositeHealth composite = Health.compositeBuilder()
                .module("db", b -> b.node().status(HealthStatus.UP))
                .module("db", b -> b.node().status(HealthStatus.DOWN))
                .build();

        // Act
        Health previous = composite.addModuleHealth("db", new HealthNode(HealthStatus.UP));

        // Assert
        assertThat(previous.status()).isEqualTo(HealthStatus.DOWN);
        assertThat(composite.getModuleHealth("db"))
                .isNotNull()
                .satisfies(h -> assertThat(h.status()).isEqualTo(HealthStatus.UP));
    }
}
