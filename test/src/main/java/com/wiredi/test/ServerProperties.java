package com.wiredi.test;

import com.wiredi.annotations.properties.Properties;
import com.wiredi.annotations.properties.PropertyName;

@Properties(
    prefix = "my.namespace.path.server"
)
public class ServerProperties {

    private final String url;
    private final int aliveTimeSeconds;

    public ServerProperties(@PropertyName(value = "somOtherString", format = false) String url, int aliveTimeSeconds) {
        this.url = url;
        this.aliveTimeSeconds = aliveTimeSeconds;
    }

    // Getters and other methods
}