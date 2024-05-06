package com.wiredi.runtime.lang;

public final class Booleans {

    /**
     * Parses a String to a boolean
     * <p>
     * The string can be either true, 1, false or 0 in any lower or upper case combination.
     * <p>
     * If the String cannot be parsed, an IllegalArgumentException is raised. This means
     * that the provided string always has to be a concrete boolean and cannot falsely be
     * interpreted as false if you don't want to.
     *
     * @param string the value to parse
     * @return true if the value is 1/true or false if the value is 0/false.
     * @throws IllegalArgumentException if the string cannot be strictly parsed.
     */
    public static boolean parseStrict(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Null cannot be parsed as boolean");
        }
        return switch (string.toLowerCase()) {
            case "1", "true" -> true;
            case "0", "false" -> false;
            default -> throw new IllegalArgumentException("The value " + string + " cannot be parsed as a boolean");
        };
    }
}
