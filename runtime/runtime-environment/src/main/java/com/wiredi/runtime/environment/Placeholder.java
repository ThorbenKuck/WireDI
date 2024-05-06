package com.wiredi.runtime.environment;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Placeholder {

    @Nullable
    private final Character identifierChar;
    private final String start;
    private final String content;
    @Nullable
    private final Placeholder.Default defaultValue;
    private final String end;
    private final PlaceholderResolver parent;
    private final int relativeStart;
    private final int relativeStop;

    public Placeholder(
            @Nullable Character identifierChar,
            String start,
            String content,
            @Nullable Placeholder.Default defaultValue,
            String end,
            PlaceholderResolver parent,
            int relativeStart, int relativeStop) {
        this.start = start;
        this.end = end;
        this.parent = parent;
        this.relativeStart = relativeStart;
        this.relativeStop = relativeStop;
        this.content = content;
        this.defaultValue = defaultValue;
        this.identifierChar = identifierChar;
    }

    public Optional<Character> getIdentifierChar() {
        return Optional.ofNullable(identifierChar);
    }

    public Optional<Default> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public String getStart() {
        return start;
    }

    public String getContent() {
        return content;
    }

    public String getEnd() {
        return end;
    }

    public PlaceholderResolver getParent() {
        return parent;
    }

    public int getRelativeStart() {
        return relativeStart;
    }

    public int getRelativeStop() {
        return relativeStop;
    }

    public String compile() {
        StringBuilder stringBuilder = new StringBuilder();
        if (identifierChar != null) {
            stringBuilder.append(identifierChar);
        }

        stringBuilder.append(start).append(content);

        if (defaultValue != null) {
            stringBuilder.append(defaultValue.delimiter).append(defaultValue.content);
        }
        return stringBuilder.append(end).toString();

    }

    public String replaceRelativeIn(String wholeString, String replacement) {
        String start = wholeString.substring(0, relativeStart);
        String stop = wholeString.substring(relativeStop);
        return start + replacement + stop;
    }

    public String replaceIn(String wholeString, String replacement) {
        return wholeString.replace(compile(), replacement);
    }

    @Nullable
    public String tryReplacementValueWithDefault(String value) {
        return Optional.ofNullable(defaultValue)
                .map(it -> replaceIn(value, it.content))
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placeholder that = (Placeholder) o;

        return relativeStart == that.relativeStart
                && relativeStop == that.relativeStop
                && Objects.equals(identifierChar, that.identifierChar)
                && Objects.equals(start, that.start)
                && Objects.equals(content, that.content)
                && Objects.equals(end, that.end)
                && Objects.equals(defaultValue, that.defaultValue)
                && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifierChar, start, content, end, defaultValue, parent, relativeStart, relativeStop);
    }

    @Override
    public String toString() {
        return compile();
    }

    public record Default(
            String content,
            String delimiter,
            PlaceholderResolver parent
    ) {
        @Override
        public String toString() {
            return delimiter + content;
        }

        public Optional<Placeholder> asPlaceholder() {
            List<Placeholder> placeholders = parent.resolveAllIn(content);

            if (placeholders.size() == 1) {
                return Optional.ofNullable(placeholders.get(0));
            }

            return Optional.empty();
        }
    }
}
