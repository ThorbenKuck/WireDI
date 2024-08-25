package com.wiredi.runtime.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlaceholderBuilder {

    private final PlaceholderResolver parent;
    private final StringBuilder start = new StringBuilder(5);
    private final StringBuilder innerContent = new StringBuilder();
    private final StringBuilder parameterDelimiter = new StringBuilder(5);
    private final StringBuilder currentParameter = new StringBuilder();
    private final List<Placeholder.Parameter> parameters = new ArrayList<>();
    private final StringBuilder end = new StringBuilder(5);
    private int relativeStart = -1;
    private Character identifier = null;
    private int relativeEnd = -1;
    private StringBuilder innerContentPointer = innerContent;
    private int depth = 0;

    public PlaceholderBuilder(PlaceholderResolver parent) {
        this.parent = parent;
    }

    public PlaceholderBuilder noteStart(
            String start,
            char identifier,
            int relativeStart
    ) {
        if (this.isActive()) {
            appendToInnerContent(start);
        } else {
            this.start.append(start);
            this.relativeStart = relativeStart;
            this.identifier = identifier;
        }
        this.depth++;
        return this;
    }

    public PlaceholderBuilder noteEnd(Consumer<Integer> consumer) {
        if (this.depth == 1 && !this.parameterDelimiter.isEmpty()) {
            this.parameters.add(new Placeholder.Parameter(
                    this.currentParameter.toString(),
                    parameterDelimiter.toString(),
                    parent
            ));
        }
        if (this.depth != 0) {
            this.depth--;

            consumer.accept(this.depth);
        }

        return this;
    }

    public boolean isActive() {
        return depth > 0;
    }

    public boolean isNotActive() {
        return depth == 0;
    }

    public int depth() {
        return this.depth;
    }

    public PlaceholderBuilder startOfParametersValue(String delimiter) {
        if (this.parameterDelimiter.isEmpty()) {
            this.parameterDelimiter.append(delimiter);
            this.innerContentPointer = currentParameter;
        } else if (depth == 1) {
            this.parameters.add(new Placeholder.Parameter(
                    this.currentParameter.toString(),
                    delimiter,
                    parent
            ));
            this.currentParameter.setLength(0);
        } else {
            this.innerContentPointer.append(delimiter);
        }

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

    public PlaceholderBuilder appendToEnd(String s, int relativeEnd) {
        this.end.append(s);
        this.relativeEnd = relativeEnd;
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
                new ArrayList<>(parameters),
                end.toString(),
                parent,
                relativeStart, relativeEnd);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (identifier != null) {
            result.append(identifier);
        }
        return result.append(start)
                .append(innerContent)
                .append(parameterDelimiter)
                .append(currentParameter)
                .append(end)
                .toString();
    }

    public void reset() {
        relativeStart = -1;
        relativeEnd = -1;
        depth = 0;
        identifier = null;
        innerContentPointer = innerContent;
        start.setLength(0);
        innerContent.setLength(0);
        parameterDelimiter.setLength(0);
        currentParameter.setLength(0);
        end.setLength(0);
        parameters.clear();
    }

    public void clear() {
        relativeStart = -1;
        relativeEnd = -1;
        depth = 0;
        identifier = null;
        innerContentPointer = null;
        start.delete(0, start.length());
        innerContent.delete(0, innerContent.length());
        parameterDelimiter.delete(0, parameterDelimiter.length());
        currentParameter.delete(0, currentParameter.length());
        end.delete(0, end.length());
        parameters.clear();
    }
}
