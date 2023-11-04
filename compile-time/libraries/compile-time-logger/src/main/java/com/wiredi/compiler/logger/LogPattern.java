package com.wiredi.compiler.logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogPattern {

	private static final String DEFAULT_LOGGING_PATTERN = "[${level:5.5}] [${thread:12.12}] [${type:30.30}] [${annotation:10.10}] [${origin:20.20}] : ${message}";
	public static final LogPattern DEFAULT = new LogPattern(DEFAULT_LOGGING_PATTERN);
	private final Map<String, Object> context = new HashMap<>();
	private final String pattern;

	public LogPattern(String pattern) {
		this.pattern = pattern;
	}

	public LogPattern newInstance() {
		return new LogPattern(pattern);
	}

	public LogPattern context(LogEntry logEntry) {
		return context("level", logEntry.logLevel())
				.context("thread", Thread.currentThread().getName())
				.context("type", logEntry.loggerType().getSimpleName())
				.context("annotation", Optional.ofNullable(logEntry.annotationType()).map(Class::getSimpleName).orElse(""))
				.context("origin", Optional.ofNullable(logEntry.targetElement()).map(it -> it.getKind().name() + " " + it.getSimpleName()).orElse(""))
				.context("message", logEntry.message());
	}

	public LogPattern context(String key, Object value) {
		context.put(key, value);
		return this;
	}

	public CompiledLogPattern compile() {
		final StringBuilder newTemplate = new StringBuilder(pattern);
		final List<Object> valueList = new ArrayList<>();
		final Matcher matcher = Pattern.compile("[$#][{][a-zA-Z0-9:.\\-_+]+[}]{1}").matcher(pattern);

		while (matcher.find()) {
			final String group = matcher.group();
			final char operator = group.charAt(0);
			String key = group.substring(2, group.length() - 1);
			String replacement = "%s";
			final String[] entries = key.split(":");
			if (entries.length > 1) {
				key = entries[0];
				replacement = "%" + entries[1] + "s";
			}

			final int index = newTemplate.indexOf(group);
			if (index != -1) {
				final String targetReplacement = replacement;
				if (operator == '#') {
					Optional.ofNullable(context.get(key))
							.ifPresent(value -> {
								newTemplate.replace(index, index + group.length(), targetReplacement);
								valueList.add(value);
							});
				} else if (operator == '$') {
					newTemplate.replace(index, index + group.length(), replacement);
					valueList.add(context.getOrDefault(key, group));
				} else {
					throw new IllegalStateException("Unable to resolve operator " + operator + " in " + group);
				}
			}
		}

		return new CompiledLogPattern(newTemplate.toString(), valueList);
	}
}
