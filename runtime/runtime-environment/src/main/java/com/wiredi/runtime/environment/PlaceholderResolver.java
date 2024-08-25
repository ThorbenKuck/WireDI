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

    public PlaceholderResolver(
            String start,
            String stop
    ) {
        this(start, stop, DEFAULT_PARAMETER_DELIMITER);
    }

    public PlaceholderResolver(
            String start,
            String stop,
            String parameterDelimiter
    ) {
        this.start = start;
        this.stop = stop;
        this.parameterDelimiter = parameterDelimiter;
    }

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
