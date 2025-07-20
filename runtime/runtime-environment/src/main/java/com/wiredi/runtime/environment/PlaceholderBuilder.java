package com.wiredi.runtime.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A builder class for creating {@link Placeholder} instances.
 * <p>
 * This class is used by {@link PlaceholderResolver} to incrementally build placeholder
 * objects while parsing input strings. It maintains the state of the current placeholder
 * being built, including its identifier, start and end delimiters, expression content,
 * parameters, and position information.
 * <p>
 * The builder supports nested placeholders and parameters, and provides methods for
 * adding different parts of a placeholder during the parsing process.
 *
 * @see Placeholder
 * @see PlaceholderResolver
 */
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

    /**
     * Creates a new PlaceholderBuilder with the specified parent resolver.
     *
     * @param parent the parent PlaceholderResolver that will use this builder
     */
    public PlaceholderBuilder(PlaceholderResolver parent) {
        this.parent = parent;
    }

    /**
     * Marks the start of a placeholder.
     * <p>
     * This method is called when a start delimiter is encountered during parsing.
     * If the builder is already active (building a placeholder), the start delimiter
     * is treated as part of the inner content of the current placeholder.
     *
     * @param start the start delimiter string
     * @param identifier the identifier character preceding the start delimiter
     * @param relativeStart the position of the identifier in the original string
     * @return this builder instance for method chaining
     */
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

    /**
     * Marks the end of a placeholder.
     * <p>
     * This method is called when an end delimiter is encountered during parsing.
     * It handles parameter finalization and depth tracking for nested placeholders.
     *
     * @param consumer a consumer that receives the current depth after decrementing
     * @return this builder instance for method chaining
     */
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

    /**
     * Builds and returns a new Placeholder instance from the current state of this builder.
     * <p>
     * This method creates a new Placeholder with all the components that have been
     * added to this builder.
     *
     * @return a new Placeholder instance
     */
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

    /**
     * Returns a string representation of the current state of this builder.
     * <p>
     * This method is primarily used for debugging purposes.
     *
     * @return a string representation of this builder
     */
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

    /**
     * Resets this builder to its initial state.
     * <p>
     * This method clears all content and resets all state variables, allowing
     * the builder to be reused for building a new placeholder.
     */
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

    /**
     * Completely clears this builder and releases references.
     * <p>
     * This method is more thorough than {@link #reset()} and is typically
     * called when the builder will no longer be used.
     */
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
