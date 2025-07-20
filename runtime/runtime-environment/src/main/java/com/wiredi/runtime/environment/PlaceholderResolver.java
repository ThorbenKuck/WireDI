package com.wiredi.runtime.environment;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for parsing and resolving placeholders in strings.
 * <p>
 * A placeholder is a special syntax in a string that can be replaced with a value.
 * Placeholders have a specific format defined by start and end delimiters, and can
 * optionally include an identifier character and parameters.
 * <p>
 * For example, in the string "Hello ${name}", "${name}" is a placeholder with
 * start delimiter "{", end delimiter "}", identifier character "$", and expression "name".
 * <p>
 * This resolver can find all placeholders in a string and create {@link Placeholder}
 * objects that can be used to resolve and replace the placeholders with actual values.
 *
 * @see Placeholder
 */
public class PlaceholderResolver {

    private static final String DEFAULT_PARAMETER_DELIMITER = ":";
    private final String start;
    private final String stop;
    private final String parameterDelimiter;

    /**
     * Creates a new PlaceholderResolver with the specified start and stop delimiters,
     * using the default parameter delimiter.
     *
     * @param start the start delimiter for placeholders (e.g., "{")
     * @param stop the end delimiter for placeholders (e.g., "}")
     */
    public PlaceholderResolver(
            String start,
            String stop
    ) {
        this(start, stop, DEFAULT_PARAMETER_DELIMITER);
    }

    /**
     * Creates a new PlaceholderResolver with the specified start, stop, and parameter delimiters.
     *
     * @param start the start delimiter for placeholders (e.g., "{")
     * @param stop the end delimiter for placeholders (e.g., "}")
     * @param parameterDelimiter the delimiter used to separate the expression from parameters (e.g., ":")
     */
    public PlaceholderResolver(
            String start,
            String stop,
            String parameterDelimiter
    ) {
        this.start = start;
        this.stop = stop;
        this.parameterDelimiter = parameterDelimiter;
    }

    /**
     * Finds and returns all placeholders in the input string.
     * <p>
     * This method parses the input string and identifies all placeholders according to
     * the configured delimiters. It handles nested placeholders and parameters correctly.
     *
     * @param input the string to search for placeholders
     * @return a list of all placeholders found in the input string
     */
    public final List<Placeholder> resolveAllIn(String input) {
        int index = 0;
        final PlaceholderBuilder builder = new PlaceholderBuilder(this);
        final List<Placeholder> placeholders = new ArrayList<>();
        final int startLength = start.length();
        final int stopLength = stop.length();

        while (index < input.length()) {
            if (input.startsWith(start, index)) {
                int relativeStart =  index - 1;
                if (index > 0 && input.charAt(relativeStart) != ' ') {
                    builder.noteStart(start, input.charAt(relativeStart), relativeStart);
                } else if(builder.isActive()) {
                    builder.appendToInnerContent(start);
                }
                index += startLength;
            } else if (builder.isActive()) {
                if (input.startsWith(stop, index)) {
                    final int finalI = index;
                    builder.noteEnd((depth) -> {
                        if (depth == 0) {
                            placeholders.add(
                                    builder.appendToEnd(stop , finalI + stopLength)
                                            .build()
                            );
                            builder.reset();
                        } else if(builder.depth() > 0) {
                            builder.appendToInnerContent(stop);
                        }
                    });

                    index += stopLength;
                } else {
                    if (input.startsWith(parameterDelimiter, index)) {
                        builder.startOfParametersValue(parameterDelimiter);
                        index += parameterDelimiter.length();
                    } else {
                        builder.appendToInnerContent(input.charAt(index));
                        index++;
                    }
                }
            } else if (input.startsWith(start, index)) {
                int relativeStart =  index - 1;
                if (index > 0 && input.charAt(relativeStart) != ' ') {
                    builder.noteStart(start, input.charAt(relativeStart), relativeStart);
                }
                index += startLength;
            } else {
                index += 1;
            }
        }

        builder.clear();
        return placeholders;
    }
}
