package com.wiredi.compiler.logger.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedPattern {

    private final List<PatternSegment> segments = new ArrayList<>();

    public ParsedPattern(List<PatternSegment> segments) {
        this.segments.addAll(segments);
    }

    // z.B. formatiert das Pattern komplett mit context
    public CompiledLogPattern compile(Map<String, Object> context) {
        StringBuilder result = new StringBuilder();
        List<Object> values = new ArrayList<>();

        for (PatternSegment segment : segments) {
            segment.appendFormatted(result, values, context);
        }

        return new CompiledLogPattern(result.toString(), values);
    }

    public static ParsedPattern parse(String pattern) {
        List<PatternSegment> segments = new ArrayList<>();
        Matcher matcher = Pattern.compile("([$#])\\{([a-zA-Z0-9._:\\-+]+)\\}").matcher(pattern);

        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                segments.add(new LiteralSegment(pattern.substring(lastIndex, matcher.start())));
            }

            char operator = matcher.group(1).charAt(0);
            String key = matcher.group(2);

            // Default values
            boolean cutRight = false;
            Integer minLength = null;
            Integer maxLength = null;

            String rawKey = key;
            String formatSpec;

            String[] parts = key.split(":");
            if (parts.length > 1) {
                rawKey = parts[0];
                formatSpec = parts[1];

                if (formatSpec.startsWith("r") || formatSpec.startsWith("l")) {
                    cutRight = formatSpec.startsWith("r");
                    formatSpec = formatSpec.substring(1);
                }

                String[] minMax = formatSpec.split("\\.");
                if (minMax.length == 2) {
                    try {
                        minLength = Integer.parseInt(minMax[0]);
                        maxLength = Integer.parseInt(minMax[1]);
                    } catch (NumberFormatException ignored) {}
                } else if (minMax.length == 1) {
                    try {
                        maxLength = Integer.parseInt(minMax[0]);
                    } catch (NumberFormatException ignored) {}
                }
            }

            segments.add(new VariableSegment(operator, rawKey, cutRight, minLength, maxLength));

            lastIndex = matcher.end();
        }

        if (lastIndex < pattern.length()) {
            segments.add(new LiteralSegment(pattern.substring(lastIndex)));
        }

        return new ParsedPattern(segments);
    }

}