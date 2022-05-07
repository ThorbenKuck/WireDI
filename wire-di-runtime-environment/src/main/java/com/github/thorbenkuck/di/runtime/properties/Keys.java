package com.github.thorbenkuck.di.runtime.properties;

import java.util.regex.Pattern;

public class Keys {

    public static final Pattern NO_UPPER_CASE_LETTER = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");

    private static boolean containsUpperCaseLetter(String key) {
        return !NO_UPPER_CASE_LETTER.matcher(key).matches();
    }

    private static String camelToUnderlinedName(String name) {
        StringBuilder sb = new StringBuilder();
        boolean lastWasUnderline = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean isCaps = Character.isUpperCase(c);
            if (isCaps) {
                if (i > 0 && !lastWasUnderline) {
                    sb.append('_');
                    lastWasUnderline = true;
                }
                c = Character.toLowerCase(c);
            }
            sb.append(c);

            lastWasUnderline = (c == '_');
        }
        return sb.toString();
    }

    public static String format(String rawKey) {
        String normalized = rawKey.replaceAll("-", "_");
        if (containsUpperCaseLetter(rawKey)) {
            return camelToUnderlinedName(normalized);
        } else {
            return rawKey;
        }
    }
}
