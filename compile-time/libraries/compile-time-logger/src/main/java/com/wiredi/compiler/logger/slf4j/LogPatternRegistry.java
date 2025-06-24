package com.wiredi.compiler.logger.slf4j;

import com.wiredi.compiler.logger.LogPattern;

import java.util.HashMap;
import java.util.Map;

public class LogPatternRegistry implements LoggerRegistry<LogPattern> {

    private final Map<String, LogPattern> patternMap = new HashMap<>();
    private LogPattern defaultLogPattern = LogPattern.DEFAULT;

    @Override
    public void setDefault(LogPattern value) {
        defaultLogPattern = value;
    }

    @Override
    public void set(String pattern, LogPattern value) {
        patternMap.put(pattern, value);
    }

    @Override
    public LogPattern get(String name) {
        // Vollständiger Match > Wildcard Match > Default
        if (patternMap.containsKey(name)) {
            return patternMap.get(name);
        }
        for (Map.Entry<String, LogPattern> entry : patternMap.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("*") && name.startsWith(key.substring(0, key.length() - 1))) {
                return entry.getValue();
            }
        }
        return defaultLogPattern;
    }
}
