package com.wiredi.compiler.logger.slf4j;

import java.util.HashMap;
import java.util.Map;

public class LogToConsoleRegistry implements LoggerRegistry<Boolean> {

    private final Map<String, Boolean> map = new HashMap<>();
    private boolean defaultValue = true;

    @Override
    public void setDefault(Boolean value) {
        this.defaultValue = value;
    }

    @Override
    public void set(String pattern, Boolean value) {
        this.map.put(pattern, value);
    }

    @Override
    public Boolean get(String name) {
        // Vollständiger Match > Wildcard Match > Default
        if (map.containsKey(name)) {
            return map.get(name);
        }
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("*") && name.startsWith(key.substring(0, key.length() - 1))) {
                return entry.getValue();
            }
        }
        return defaultValue;

    }
}
