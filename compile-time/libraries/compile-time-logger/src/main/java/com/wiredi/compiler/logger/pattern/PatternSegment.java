package com.wiredi.compiler.logger.pattern;

import java.util.List;
import java.util.Map;

interface PatternSegment {
    void appendFormatted(StringBuilder out, List<Object> values, Map<String, Object> context);
}