package com.wiredi.tests;

import com.wiredi.annotations.ActiveProfiles;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.beans.Bean;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WiredTest
@ActiveProfiles("test")
@Wire
public class ExampleTest {

    @Inject
    private ExampleService exampleServiceFieldInjection;

    @Inject
    private IdentifiableProvider<ExampleService> identifiableProviderFieldInjection;

    @Inject
    private Bean<ExampleService> beanFieldInjection;

    @Inject
    private WireRepository classWireRepositoryFieldInjection;

    private final ExampleService exampleServiceConstructorInjection;
    private final IdentifiableProvider<ExampleService> identifiableProviderConstructorInjection;
    private final Bean<ExampleService> beanConstructorInjection;
    private final WireRepository classWireRepositoryConstructorInjection;

    public ExampleTest(
            ExampleService exampleServiceConstructorInjection,
            IdentifiableProvider<ExampleService> identifiableProviderConstructorInjection,
            Bean<ExampleService> beanConstructorInjection,
            WireRepository classWireRepositoryConstructorInjection
    ) {
        this.exampleServiceConstructorInjection = exampleServiceConstructorInjection;
        this.identifiableProviderConstructorInjection = identifiableProviderConstructorInjection;
        this.beanConstructorInjection = beanConstructorInjection;
        this.classWireRepositoryConstructorInjection = classWireRepositoryConstructorInjection;
    }

    @Test
    public void test(WireRepository wireRepository, IdentifiableProvider<ExampleService> provider) {
        assertThat(classWireRepositoryFieldInjection).isSameAs(classWireRepositoryConstructorInjection).isSameAs(wireRepository);
        assertThat(identifiableProviderFieldInjection).isSameAs(identifiableProviderConstructorInjection).isNotNull().isSameAs(provider).isSameAs(wireRepository.getNativeProvider(ExampleService.class));
        assertThat(beanFieldInjection).isSameAs(beanConstructorInjection).isNotNull().isSameAs(wireRepository.getBean(ExampleService.class));
        assertThat(exampleServiceFieldInjection).isSameAs(exampleServiceConstructorInjection).isNotNull();
        assertThat(identifiableProviderFieldInjection.get(wireRepository))
                .isSameAs(
                        beanFieldInjection.get(TypeIdentifier.just(ExampleService.class), () -> StandardWireConflictResolver.NONE)
                                .instantiate(wireRepository, TypeIdentifier.just(ExampleService.class))
                )
                .isSameAs(exampleServiceFieldInjection);
    }
}
