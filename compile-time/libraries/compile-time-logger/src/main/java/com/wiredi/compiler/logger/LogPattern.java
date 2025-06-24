package com.wiredi.compiler.logger;

import com.wiredi.compiler.logger.pattern.CompiledLogPattern;
import com.wiredi.compiler.logger.pattern.ParsedPattern;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogPattern {

    private static final String DEFAULT_LOGGING_PATTERN = "[${level:5.5}] [${thread:12.12}] [${type:r30.30}] [${annotation:10.10}] [${origin:20.20}] : ${message}";
    private static final ParsedPattern DEFAULT_PATTERN = ParsedPattern.parse(DEFAULT_LOGGING_PATTERN);
    public static final LogPattern DEFAULT = new LogPattern(DEFAULT_PATTERN);
    private final Map<String, Object> context = new HashMap<>();
    private final ParsedPattern pattern;

    public LogPattern(ParsedPattern pattern) {
        this.pattern = pattern;
    }

    public LogPattern newInstance() {
        return new LogPattern(pattern);
    }

    public LogPattern context(String key, Object value) {
        context.put(key, value);
        return this;
    }

    public CompiledLogPattern compile() {
        return this.pattern.compile(context);
    }
}
