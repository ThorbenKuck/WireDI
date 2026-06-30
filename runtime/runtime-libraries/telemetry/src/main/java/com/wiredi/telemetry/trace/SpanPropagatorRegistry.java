package com.wiredi.telemetry.trace;

import com.wiredi.runtime.collections.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpanPropagatorRegistry {

    private final List<SpanPropagator> propagators;
    private final TypeMap<SpanPropagator> propagatorCache = new TypeMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SpanPropagatorRegistry.class);

    public SpanPropagatorRegistry() {
        this.propagators = new ArrayList<>();
    }

    public SpanPropagatorRegistry(List<SpanPropagator> propagators) {
        this.propagators = new ArrayList<>(propagators);
    }

    public boolean propagate(List<SpanTransportation> transportations, Object carrier) {
        SpanPropagator knownPropagator = propagatorCache.get(carrier.getClass());
        if (knownPropagator != null) {
            boolean success = true;
            for (SpanTransportation transportation : transportations) {
                success &= knownPropagator.propagate(transportation, carrier);
            }

            if (success) {
                return true;
            }
        }

        boolean searching = true;
        SpanPropagator propagator = null;
        for (SpanTransportation transportation : transportations) {
            SpanPropagator matching = propagate(transportation, carrier);
            if (propagator == null && searching) {
                propagator = matching;
            } else if (matching != propagator) {
                propagator = matching;
                searching = false;
            }
        }

        if (propagator != null && searching) {
            propagatorCache.put(carrier.getClass(), propagator);
        }

        return propagator != null;
    }

    private SpanPropagator propagate(SpanTransportation transportation, Object carrier) {
        for (SpanPropagator propagator : propagators) {
            if (propagator.propagate(transportation, carrier)) {
                return propagator;
            }
        }

        logger.warn("No propagator found for carrier: {} with propagation {}. Consider adding a propagator that supports this carrier to not loose spans.", carrier, transportation);
        return null;
    }
}
