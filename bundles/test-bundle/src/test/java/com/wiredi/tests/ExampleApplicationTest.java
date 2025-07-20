package com.wiredi.tests;

import com.wiredi.annotations.ActiveProfiles;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ApplicationTest
@ActiveProfiles("test")
public class ExampleApplicationTest {

    @Inject
    private ExampleService exampleServiceFieldInjection;

    @Inject
    private IdentifiableProvider<ExampleService> identifiableProviderFieldInjection;

    @Inject
    private WireContainer classWireRepositoryFieldInjection;

    private final ExampleService exampleServiceConstructorInjection;
    private final WireContainer classWireRepositoryConstructorInjection;

    public ExampleApplicationTest(
            ExampleService exampleServiceConstructorInjection,
            WireContainer classWireRepositoryConstructorInjection
    ) {
        this.exampleServiceConstructorInjection = exampleServiceConstructorInjection;
        this.classWireRepositoryConstructorInjection = classWireRepositoryConstructorInjection;
    }

    @Test
    public void test(WireContainer wireContainer) {
        IdentifiableProvider<ExampleService> provider = wireContainer.getNativeProvider(TypeIdentifier.just(ExampleService.class));
        assertThat(classWireRepositoryFieldInjection).isSameAs(classWireRepositoryConstructorInjection).isSameAs(wireContainer);
        assertThat(identifiableProviderFieldInjection).isNotNull().isSameAs(provider).isSameAs(wireContainer.getNativeProvider(TypeIdentifier.just(ExampleService.class)));
        assertThat(exampleServiceFieldInjection)
                .isSameAs(exampleServiceConstructorInjection)
                .isNotNull();
        assertThat(identifiableProviderFieldInjection.get(wireContainer))
                .withFailMessage("Instances constructed by providers should be new instances")
                .isNotSameAs(exampleServiceFieldInjection);
    }
}
