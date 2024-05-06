package com.wiredi.qualifier.input;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import com.wiredi.runtime.values.Value;
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
        QualifierType.newInstance("jakarta.inject.Named")
            .add("value", "TestClass")
            .build()
    );

    private static final TypeIdentifier<TestClass> PRIMARY_WIRE_TYPE = TypeIdentifier.of(TestClass.class);

    private final Value<TestClass> instance = Value.empty();

    private TestClass createInstance(final WireRepository wireRepository,
                                     final TypeIdentifier<TestClass> concreteType) {
        TestClass instance = new TestClass();
        return instance;
    }

    @Override
    public final synchronized TestClass get(@NotNull final WireRepository wireRepository,
                                            @NotNull final TypeIdentifier<TestClass> concreteType) {
        return instance.getOrSet(() -> createInstance(wireRepository, concreteType));
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
