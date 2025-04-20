package com.wiredi.metrics;

public record MetricTag(String key, String value) {

    public static MetricTag of(String key, String value) {
        return new MetricTag(key, value);
    }

}
