package com.wiredi.compiler.logger.pattern;

import java.util.List;
import java.util.Map;

class VariableSegment implements PatternSegment {
    private final char operator;       // $ oder #
    private final String key;
    private final boolean cutRight;    // r oder l
    private final Integer minLength;
    private final Integer maxLength;

    public VariableSegment(char operator, String key, boolean cutRight, Integer minLength, Integer maxLength) {
        this.operator = operator;
        this.key = key;
        this.cutRight = cutRight;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public void appendFormatted(StringBuilder out, List<Object> values, Map<String, Object> context) {
        Object rawValue = context.get(key);
        if (rawValue == null) {
            // Wenn kein Wert, Variable als Text rein
            out.append(operator).append('{').append(key).append('}');
            return;
        }
        String valueStr = rawValue.toString();

        // Abschneiden
        if (maxLength != null && valueStr.length() > maxLength) {
            if (cutRight) {
                valueStr = valueStr.substring(valueStr.length() - maxLength);
            } else {
                valueStr = valueStr.substring(0, maxLength);
            }
        }

        // Padding
        if (minLength != null && valueStr.length() < minLength) {
            int padLength = minLength - valueStr.length();
            String padding = " ".repeat(padLength);

            if (cutRight) {
                valueStr = padding + valueStr;
            } else {
                valueStr = valueStr + padding;
            }
        }

        // %s als Platzhalter
        out.append("%s");
        values.add(valueStr);
    }
}