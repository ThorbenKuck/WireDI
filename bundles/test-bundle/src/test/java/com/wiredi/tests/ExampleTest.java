package com.wiredi.tests;

import com.wiredi.annotations.Wire;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.WireRepository;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WireTest
@Wire
public class ExampleTest {

    @Inject
    public ExampleService exampleService;

    @Inject
    public IdentifiableProvider<ExampleService> identifiableProvider;

    @Inject
    public WireRepository classWireRepository;

    @Test
    public void test(WireRepository wireRepository, IdentifiableProvider<ExampleService> provider) {
        assertThat(classWireRepository).isSameAs(wireRepository);
        assertThat(identifiableProvider).isNotNull();
        assertThat(exampleService).isNotNull();
        assertThat(provider).isSameAs(identifiableProvider);
        assertThat(identifiableProvider.get(wireRepository)).isSameAs(exampleService);
    }
}
