package com.wiredi.telemetry.trace;

public interface SpanPropagator {

    boolean propagate(SpanTransportation transportations, Object carrier);

}
