package com.wiredi.environment;

import java.util.function.Consumer;

public class PlaceholderBuilder {

    private final PlaceholderResolver parent;
    private final StringBuilder start = new StringBuilder(5);
    private final StringBuilder innerContent = new StringBuilder();
    private final StringBuilder defaultValueDelimiter = new StringBuilder(5);
    private final StringBuilder defaultValue = new StringBuilder();
    private final StringBuilder end = new StringBuilder(5);
    private int relativeStart = -1;
    private Character identifier = null;
    private int relativeEnd = -1;
    private StringBuilder innerContentPointer = innerContent;
    private int depth = 0;

    public PlaceholderBuilder(PlaceholderResolver parent) {
        this.parent = parent;
    }

    public void noteStart() {
        this.depth++;
    }

    public void noteEnd(Consumer<Integer> consumer) {
        if (this.depth != 0) {
            this.depth--;

            consumer.accept(this.depth);
        }
    }

    public int depth() {
        return this.depth;
    }

    public PlaceholderBuilder withIdentifier(Character identifier) {
        this.identifier = identifier;
        return this;
    }

    public PlaceholderBuilder startOfDefaultValue(String delimiter) {
        if (this.defaultValueDelimiter.isEmpty()) {
            this.defaultValueDelimiter.append(delimiter);
            this.innerContentPointer = defaultValue;
        } else {
            this.innerContentPointer.append(delimiter);
        }

        return this;
    }

    public PlaceholderBuilder withRelativeStart(int relativeStart) {
        this.relativeStart = relativeStart;
        return this;
    }

    public PlaceholderBuilder withRelativeEnd(int relativeEnd) {
        this.relativeEnd = relativeEnd;
        return this;
    }

    public PlaceholderBuilder appendToStart(String s) {
        this.start.append(s);
        return this;
    }

    public PlaceholderBuilder appendToStart(char c) {
        this.start.append(c);
        return this;
    }

    public PlaceholderBuilder appendToInnerContent(String c) {
        this.innerContentPointer.append(c);
        return this;
    }

    public PlaceholderBuilder appendToInnerContent(char c) {
        this.innerContentPointer.append(c);
        return this;
    }

    public PlaceholderBuilder appendToEnd(String s) {
        this.end.append(s);
        return this;
    }

    public PlaceholderBuilder appendToEnd(char c) {
        this.end.append(c);
        return this;
    }

    public Placeholder build() {
        return new Placeholder(
                identifier,
                start.toString(),
                innerContent.toString(),
                buildDefault(),
                end.toString(),
                parent,
                relativeStart, relativeEnd);
    }

    private Placeholder.Default buildDefault() {
        if (defaultValueDelimiter.isEmpty()) {
            return null;
        } else {
            return new Placeholder.Default(defaultValue.toString(), defaultValueDelimiter.toString(), parent);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (identifier != null) {
            result.append(identifier);
        }
        return result.append(start).append(innerContent).append(defaultValueDelimiter).append(defaultValue).append(end).toString();
    }

    public void reset() {
        relativeStart = -1;
        relativeEnd = -1;
        depth = 0;
        identifier = null;
        innerContentPointer = innerContent;
        start.setLength(0);
        innerContent.setLength(0);
        defaultValueDelimiter.setLength(0);
        defaultValue.setLength(0);
        end.setLength(0);
    }

    public void clear() {
        relativeStart = -1;
        relativeEnd = -1;
        depth = 0;
        identifier = null;
        innerContentPointer = null;
        start.delete(0, start.length());
        innerContent.delete(0, innerContent.length());
        defaultValueDelimiter.delete(0, defaultValueDelimiter.length());
        defaultValue.delete(0, defaultValue.length());
        end.delete(0, end.length());
    }
}
