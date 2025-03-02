package com.wiredi.runtime.beans;

import com.wiredi.annotations.ManualWireCandidate;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.PropertyKeys;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.values.Value;

import java.util.function.Supplier;

@ManualWireCandidate
public class BeanContainerProperties {

    private final Value<Integer> conditionalRoundThreshold;
    private final Value<Supplier<WireConflictResolver>> wireConflictResolverSupplier;

    public BeanContainerProperties(Environment environment) {
        this.conditionalRoundThreshold = Value.lazy(() -> environment.getProperty(PropertyKeys.CONDITIONAL_ROUND_THRESHOLD.getKey(), 10));
        this.wireConflictResolverSupplier = Value.lazy(() -> environment.getProperty(PropertyKeys.WIRE_CONFLICT_RESOLVER.getKey(), () -> StandardWireConflictResolver.DEFAULT));
    }

    public BeanContainerProperties(int conditionalRoundThreshold, Supplier<WireConflictResolver> wireConflictResolverSupplier) {
        this.conditionalRoundThreshold = Value.just(conditionalRoundThreshold);
        this.wireConflictResolverSupplier = Value.just(wireConflictResolverSupplier);
    }

    public Supplier<WireConflictResolver> wireConflictResolverSupplier() {
        return wireConflictResolverSupplier.get();
    }

    public BeanContainerProperties withConflictResolver(WireConflictResolver conflictResolver) {
        this.wireConflictResolverSupplier.set(() -> conflictResolver);
        return this;
    }

    public int conditionalRoundThreshold() {
        return conditionalRoundThreshold.get();
    }

    public BeanContainerProperties withConditionalRoundThreshold(int conditionalRoundThreshold) {
        this.conditionalRoundThreshold.set(conditionalRoundThreshold);
        return this;
    }
}
