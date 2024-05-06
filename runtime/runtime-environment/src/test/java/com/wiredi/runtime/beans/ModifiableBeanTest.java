package com.wiredi.runtime.beans;

import com.wiredi.annotations.properties.Name;
import com.wiredi.runtime.beans.value.BeanValue;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.provider.AbstractIdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.infrastructure.GenericBase;
import com.wiredi.runtime.infrastructure.GenericBaseIdentifiableProvider;
import com.wiredi.runtime.qualifier.QualifierType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModifiableBeanTest {

    ModifiableBean<GenericBase> modifiableBean;
    GenericBaseIdentifiableProvider<String> stringProvider = GenericBase.provider("Test");
    AbstractIdentifiableProvider<GenericBase> qualifiedStringProvider = GenericBase.provider("Qualified")
            .eraseGeneric()
            .withQualifier(QualifierType.just(Name.class));
    GenericBaseIdentifiableProvider<Integer> intProvider = GenericBase.provider(1);

    @BeforeEach
    void setup() {
        modifiableBean = new ModifiableBean<>(TypeIdentifier.of(GenericBase.class));
        modifiableBean.register(TypeIdentifier.just(GenericBase.class).withGeneric(String.class), stringProvider.eraseGeneric());
        modifiableBean.register(TypeIdentifier.just(GenericBase.class).withGeneric(String.class), qualifiedStringProvider);
        modifiableBean.register(TypeIdentifier.just(GenericBase.class).withGeneric(Integer.class), intProvider.eraseGeneric());
    }

    @Test
    void whenRequestingClassesWithSpecificGenericTheseInstancesAreReturned() {
        // Act
        BeanValue<GenericBase> genericBaseBeanValue = modifiableBean.get(TypeIdentifier.just(GenericBase.class).withGeneric(String.class), () -> StandardWireConflictResolver.NONE);

        // Assert
        assertThat(genericBaseBeanValue.isPresent()).isTrue();
        IdentifiableProvider<GenericBase> provider = genericBaseBeanValue.orElseThrow(() -> new AssertionFailedError(""));
        assertThat(provider).isSameAs(stringProvider);
    }

    @Test
    void whenRequestingAllClassesAllIdentifiersAreReturned() {
        // Act
        assertThat(modifiableBean.getAll()).containsExactlyInAnyOrder(stringProvider.eraseGeneric(), qualifiedStringProvider, intProvider.eraseGeneric());
    }

    @Test
    void whenRequestingAllClassesWithStringGenericAllIdentifiersHaveAStringGenericAreReturned() {
        // Act
        List<IdentifiableProvider<GenericBase>> providers = modifiableBean.getAll(TypeIdentifier.just(GenericBase.class).withGeneric(String.class));

        // Assert
        assertThat(providers).containsExactlyInAnyOrder(stringProvider.eraseGeneric(), qualifiedStringProvider);
    }

    @Test
    void whenRequestingAllClassesWithIntGenericAllIdentifiersHaveAIntGenericAreReturned() {
        // Act
        List<IdentifiableProvider<GenericBase>> providers = modifiableBean.getAll(TypeIdentifier.just(GenericBase.class).withGeneric(Integer.class));

        // Assert
        assertThat(providers).containsExactlyInAnyOrder(intProvider.eraseGeneric());
    }
}
