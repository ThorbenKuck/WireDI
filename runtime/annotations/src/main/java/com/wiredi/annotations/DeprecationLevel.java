package com.wiredi.annotations;

public enum DeprecationLevel {
    /**
     * The property is deprecated but still functional.
     * Typically displayed as a warning in the IDE.
     */
    WARNING("warning"),

    /**
     * The property is no longer supported and should not be used.
     * Typically displayed as an error in the IDE.
     */
    ERROR("error");

    private final String value;

    DeprecationLevel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DeprecationLevel fromValue(String value) {
        for (DeprecationLevel level : values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown deprecation level: " + value);
    }
}
