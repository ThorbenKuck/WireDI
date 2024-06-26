package com.wiredi.processor.tck.domain.generics;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.beans.Bean;
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
    private final PrimaryDoubleImpl primaryDoubleImpl;

    private final Bean<GenericBase<Double>> doubleBean;
    private final Provider<GenericBase<Double>> doubleProvider;
    private final IdentifiableProvider<GenericBase<Double>> nativeDoubleProvider;
    @Inject
    GenericBase<String> stringBase;

    @Inject
    GenericBase<Long> longBase;

    @Inject
    GenericBase<Double> doubleBase;

    private final WireRepository wireRepository;

    public GenericTestCase(
            List<GenericBase<?>> generics, // Todo: Support wildcard generics. Should be the same as no generics
            DoubleImpl doubleimpl,
            PrimaryDoubleImpl primaryDoubleImpl,
            Bean<GenericBase<Double>> doubleBean,
            Provider<GenericBase<Double>> doubleProvider,
            IdentifiableProvider<GenericBase<Double>> nativeDoubleProvider,
            WireRepository wireRepository
    ) {
        this.generics = generics;
        this.doubleimpl = doubleimpl;
        this.primaryDoubleImpl = primaryDoubleImpl;
        this.doubleBean = doubleBean;
        this.doubleProvider = doubleProvider;
        this.nativeDoubleProvider = nativeDoubleProvider;
        this.wireRepository = wireRepository;
    }

    @Override
    public Collection<DynamicNode> dynamicTests() {
        return List.of(
                dynamicTest("Assert that 5 GenericBases are wired", () -> assertThat(generics).hasSize(5)),
                dynamicTest("Assert that the generic String implementation is wired correctly", () -> assertThat(stringBase).isNotNull()),
                dynamicTest("Assert that the generic Long implementation is wired correctly", () -> assertThat(longBase).isNotNull()),
                dynamicTest("Assert that the generic Double implementation is wired correctly", () -> assertThat(doubleBase).isNotNull()),
                dynamicTest("Assert that the the primary String implementation is wired to the primary instance", () -> assertThat(stringBase).isInstanceOf(PrimaryStringImpl.class)),
                dynamicTest("Assert that the the primary Double implementation is taken from the configuration and wired from the instance", () -> assertThat(doubleBase).isInstanceOf(PrimaryDoubleImpl.class)),
                dynamicTest("Assert that the the double field is equal to double native provider", () -> assertThat(doubleBase).isSameAs(nativeDoubleProvider.get(wireRepository))),
                dynamicTest("Assert that the the double field is equal to double provider", () -> assertThat(doubleBase).isSameAs(doubleProvider.get())),
                dynamicTest("Assert that the the double native provider is equal to double provider", () -> assertThat(nativeDoubleProvider.get(wireRepository)).isSameAs(doubleProvider.get()))
        );
    }
}
