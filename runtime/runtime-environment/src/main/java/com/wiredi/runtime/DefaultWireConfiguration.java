package com.wiredi.runtime;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.DefaultConfiguration;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;

import java.time.Clock;

@DefaultConfiguration
public class DefaultWireConfiguration {

    @Provider
    @ConditionalOnMissingBean(type = Clock.class)
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
