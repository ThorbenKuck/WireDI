package com.wiredi.runtime.environment;

import java.util.ArrayList;
import java.util.List;

/**
 * This class determines placeholder values in strings.
 */
public class PlaceholderResolver {

    private static final String DEFAULT_PARAMETER_DELIMITER = ":";
    private final String start;
    private final String stop;
    private final String parameterDelimiter;

    public PlaceholderResolver(String start, String stop) {
        this(start, stop, DEFAULT_PARAMETER_DELIMITER);
    }

    public PlaceholderResolver(String start, String stop, String parameterDelimiter) {
        this.start = start;
        this.stop = stop;
        this.parameterDelimiter = parameterDelimiter;
    }

    public final List<Placeholder> resolveAllIn(String input) {
        int i = 0;
        final PlaceholderBuilder builder = new PlaceholderBuilder(this);
        final List<Placeholder> placeholders = new ArrayList<>();

        while (i < input.length()) {
            if (input.startsWith(start, i)) {
                builder.noteStart();

                if (builder.depth() == 1) {
                    // First found start
                    if (i > 0 && input.charAt(i - 1) != ' ') {
                        builder.withIdentifier(input.charAt(i - 1))
                                .withRelativeStart(i - 1);
                    } else {
                        builder.withRelativeStart(i);
                    }

                    builder.appendToStart(start);
                } else {
                    builder.appendToInnerContent(start);
                }

                i += start.length();
            } else if (input.startsWith(stop, i)) {
                int finalI = i;
                builder.noteEnd((depth) -> {
                        if (depth == 0) {
                            placeholders.add(
                                    builder.appendToEnd(stop)
                                            .withRelativeEnd(finalI + stop.length())
                                            .build()
                            );
                            builder.reset();
                        } else if(builder.depth() > 0) {
                            builder.appendToInnerContent(stop);
                        }
                    });

                i += stop.length();
            } else {
                if (builder.depth() == 0) {
                    i += start.length();
                } else {
                    if (input.startsWith(parameterDelimiter, i)) {
                        builder.startOfDefaultValue(parameterDelimiter);
                        i += parameterDelimiter.length();
                    } else {
                        builder.appendToInnerContent(input.charAt(i));
                        i++;
                    }
                }
            }

        }

        builder.clear();
        return placeholders;
    }
}
