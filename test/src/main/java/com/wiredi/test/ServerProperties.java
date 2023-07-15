package com.wiredi.test;

import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.annotations.properties.Name;

@PropertyBinding(
    prefix = "my.namespace.path.server"
)
public class ServerProperties {

    private final String url;
    private final int aliveTimeSeconds;

//    public ServerProperties(@Name(value = "somOtherString") String url, int aliveTimeSeconds) {
//        this.url = url;
//        this.aliveTimeSeconds = aliveTimeSeconds;
//    }

    public ServerProperties(@Name(value = "somOtherString") String url) {
        this.url = url;
        this.aliveTimeSeconds = 0;
    }

    // Getters and other methods
}