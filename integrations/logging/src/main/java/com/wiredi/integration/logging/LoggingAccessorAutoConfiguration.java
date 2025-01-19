package com.wiredi.integration.logging;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.logging.LoggingAccessor;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;

import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(
        key = "wiredi.jackson.autoconfigure",
        havingValue = "true",
        matchIfMissing = true
)
public class LoggingAccessorAutoConfiguration {

    @Provider
    @ConditionalOnMissingBean(type = LoggingAccessor.class)
    public LoggingAccessor createLoggingAccessor(List<LoggingAccessorConfigurer> configurers) {
        LoggingAccessor loggingAccessor = new LoggingAccessor();
        configurers.forEach(configurer -> configurer.configure(loggingAccessor));
        return loggingAccessor;
    }
}
