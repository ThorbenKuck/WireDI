package com.wiredi.compiler.logger.pattern;

import java.util.List;
import java.util.Map;

class LiteralSegment implements PatternSegment {
    private final String text;

    public LiteralSegment(String text) {
        this.text = text;
    }

    @Override
    public void appendFormatted(StringBuilder out, List<Object> values, Map<String, Object> context) {
        out.append(text);
    }
}