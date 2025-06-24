package com.wiredi.compiler.logger.slf4j;

import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LogLevelRegistry implements LoggerRegistry<Level> {

    private final Map<String, Level> levelMap = new HashMap<>();
    private Level defaultLevel = Level.INFO;

    @Override
    public void setDefault(Level value) {
        defaultLevel = value;
    }

    @Override
    public void set(String pattern, Level value) {
        levelMap.put(pattern, value);
    }

    @Override
    public Level get(String name) {
        // Complete Match > Pattern Match > Default
        if (levelMap.containsKey(name)) {
            return levelMap.get(name);
        }

        for (Map.Entry<String, Level> entry : levelMap.entrySet()) {
            if (name.startsWith(entry.getKey())) {
                return entry.getValue();
            }

            Pattern key = Pattern.compile(entry.getKey());
            if (key.matcher(name).matches()) {
                return entry.getValue();
            }
        }

        return defaultLevel;
    }
}
