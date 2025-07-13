package com.wiredi.qualifier.input;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import jakarta.annotation.Generated;
import java.lang.Override;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@Generated(
        value = "com.wiredi.compiler.domain.entities.IdentifiableProviderEntity",
        date = "2023-01-01T00:00Z"
)
@AutoService({IdentifiableProvider.class})
public final class TestClassIdentifiableProvider implements IdentifiableProvider<TestClass> {
    @NotNull
    public static final List<QualifierType> QUALIFIER = List.of(
            QualifierType.builder("jakarta.inject.Named")
                    .add("value", "TestClass")
                    .build()
    );

    private static final TypeIdentifier<TestClass> PRIMARY_WIRE_TYPE = TypeIdentifier.of(TestClass.class);

    private TestClass createInstance(final WireContainer wireRepository,
                                     final TypeIdentifier<TestClass> concreteType) {
        TestClass instance = new TestClass();
        return instance;
    }

    @Override
    public final TestClass get(@NotNull final WireContainer wireRepository,
                               @NotNull final TypeIdentifier<TestClass> concreteType) {
        return createInstance(wireRepository, concreteType);
    }

    @NotNull
    @Override
    public final List<QualifierType> qualifiers() {
        return QUALIFIER;
    }

    @Override
    @NotNull
    public final TypeIdentifier<TestClass> type() {
        return PRIMARY_WIRE_TYPE;
    }
}
