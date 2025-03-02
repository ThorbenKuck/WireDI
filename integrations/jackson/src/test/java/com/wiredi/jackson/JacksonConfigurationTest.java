package com.wiredi.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wiredi.annotations.ActiveProfiles;
import com.wiredi.integration.jackson.ObjectMapperConfigurer;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WiredApplicationInstance;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@ActiveProfiles("test")
class JacksonConfigurationTest {

    @Test
    public void verifyThatTheObjectMapperIsLoadedIntoTheWireRepository() {
        // Arrange
        WireRepository repository = WiredApplication.start().repository();

        // Act
        // Assert
        assertThat(repository.contains(ObjectMapper.class)).isTrue();
        assertThat(repository.getBean(ObjectMapper.class).isNotEmpty()).isTrue();
        assertThatCode(() -> repository.get(ObjectMapper.class)).doesNotThrowAnyException();
    }

    @Test
    public void verifyThatTheObjectMapperCanBeOverwritten() {
        // Arrange
        WireRepository repository = WireRepository.create();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.CLOSE_CLOSEABLE);
        repository.announce(IdentifiableProvider.singleton(objectMapper));
        repository.load();

        // Act
        // Assert
        assertThat(repository.getBean(ObjectMapper.class).isNotEmpty()).isTrue();
        assertThatCode(() -> repository.get(ObjectMapper.class)).doesNotThrowAnyException();
        ObjectMapper repositoryObjectMapper = repository.get(ObjectMapper.class);
        assertThat(repository.contains(ObjectMapper.class)).isTrue();
        assertThat(repositoryObjectMapper.isEnabled(SerializationFeature.CLOSE_CLOSEABLE)).isTrue();
        assertThat(repositoryObjectMapper).isSameAs(objectMapper);
    }

    @Test
    public void verifyThatTheObjectMapperCanBeDynamicallyConfigured() {
        // Arrange
        WireRepository repository = WiredApplication.start().repository();
        repository.announce(IdentifiableProvider.singleton(objectMapper -> objectMapper.enable(SerializationFeature.CLOSE_CLOSEABLE), ObjectMapperConfigurer.class));

        // Act
        // Assert
        assertThat(repository.contains(ObjectMapper.class)).isTrue();
        assertThat(repository.getBean(ObjectMapper.class).isNotEmpty()).isTrue();
        assertThatCode(() -> repository.get(ObjectMapper.class)).doesNotThrowAnyException();
        assertThat(repository.get(ObjectMapper.class).isEnabled(SerializationFeature.CLOSE_CLOSEABLE)).isTrue();
    }

    @Test
    public void verifyThatTheAutoConfigurationCanBeDisabledWhenUsingTheCorrectProperty() {
        // Arrange
        WireRepository repository = WireRepository.create();
        repository.environment().setProperty(Key.just("wiredi.jackson.autoconfigure"), "false");
        repository.load();

        // Act
        // Assert
        assertThatCode(() -> repository.get(ObjectMapper.class)).isInstanceOf(BeanNotFoundException.class);
    }

    @Test
    public void verifyThatTheObjectMapperCanBeUsedAsASingletonDependency() {
        // Arrange
        WireRepository repository = WiredApplication.start().repository();

        // Act
        // Assert
        assertThat(repository.contains(Dependency.class)).isTrue();
        assertThat(repository.getBean(Dependency.class).isNotEmpty()).isTrue();
        Dependency dependency = repository.get(Dependency.class);
        assertThat(dependency.objectMapper()).isNotNull().isSameAs(repository.get(ObjectMapper.class));
    }
}