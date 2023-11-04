package com.wiredi.environment;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PlaceholderResolver {

    private static final String DEFAULT_DELIMITER = ":";
    private final String start;
    private final String stop;

    public PlaceholderResolver(String start, String stop) {
        this.start = start;
        this.stop = stop;
    }

    public List<Placeholder> resolveAllIn(String input) {
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
                    if (input.startsWith(DEFAULT_DELIMITER, i)) {
                        builder.startOfDefaultValue(DEFAULT_DELIMITER);
                        i += DEFAULT_DELIMITER.length();
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
