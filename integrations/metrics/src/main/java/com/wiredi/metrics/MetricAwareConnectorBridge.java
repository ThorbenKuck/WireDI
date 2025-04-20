package com.wiredi.metrics;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.Eager;
import com.wiredi.runtime.lang.Ordered;

import java.util.List;

public class MetricAwareConnectorBridge implements Eager, Ordered {

    private static final Logging logger = Logging.getInstance(MetricAwareConnectorBridge.class);

    @Override
    public void setup(WireRepository wireRepository) {
        List<MetricAware> metricAwares = wireRepository.getAll(MetricAware.class);
        if (metricAwares.isEmpty()) {
            return;
        }

        wireRepository.tryGet(MetricCollector.class).ifPresent(collector -> {
            logger.debug(() -> "MetricCollector detected, setting up metric aware components.");
            metricAwares.forEach(metricAware -> metricAware.setMetricCollector(collector));
        });
    }

    @Override
    public int getOrder() {
        return FIRST + 10;
    }
}
