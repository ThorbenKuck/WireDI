package com.wiredi.processor.tck.domain.generics;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class GenericTestCase implements TckTestCase {

    private final List<GenericBase<?>> generics;

    private final DoubleImpl doubleimpl;

    private final Provider<GenericBase<Double>> doubleProvider;
    private final IdentifiableProvider<GenericBase<Double>> nativeDoubleProvider;
    @Inject
    GenericBase<String> stringBase;

    @Inject
    GenericBase<Long> longBase;

    @Inject
    GenericBase<Double> doubleBase;

    private final WireContainer wireContainer;

    public GenericTestCase(
            List<GenericBase<?>> generics,
            DoubleImpl doubleimpl,
            Provider<GenericBase<Double>> doubleProvider,
            IdentifiableProvider<GenericBase<Double>> nativeDoubleProvider,
            WireContainer wireContainer
    ) {
        this.generics = generics;
        this.doubleimpl = doubleimpl;
        this.doubleProvider = doubleProvider;
        this.nativeDoubleProvider = nativeDoubleProvider;
        this.wireContainer = wireContainer;
    }

    @Override
    public Collection<DynamicNode> dynamicTests() {
        return List.of(
                dynamicTest("Assert that 3 GenericBases are wired", () -> assertThat(generics).hasSize(3)),
                dynamicTest("Assert that the generic String implementation is wired correctly", () -> assertThat(stringBase).isNotNull()),
                dynamicTest("Assert that the generic Long implementation is wired correctly", () -> assertThat(longBase).isNotNull()),
                dynamicTest("Assert that the generic Double implementation is wired correctly", () -> assertThat(doubleBase).isNotNull())
        );
    }
}
