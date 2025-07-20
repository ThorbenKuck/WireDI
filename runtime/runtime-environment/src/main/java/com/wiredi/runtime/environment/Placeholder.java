package com.wiredi.runtime.environment;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is representing a resolvable placeholder.
 * <p>
 * Placeholders consist of two main parts: expressions and default values in the following format:
 * <p>
 * {@code <identifierChar><start><expression><defaultDelimiter><defaultValue><end>}
 * <p>
 * One concrete example of this is: {@code ${expression:defaultValue}}
 */
public class Placeholder {

    @Nullable
    private final Character identifierChar;
    private final String start;
    private final String expression;
    private final List<Parameter> parameters;
    private final String end;
    private final PlaceholderResolver parent;
    private final int relativeStart;
    private final int relativeStop;

    public Placeholder(
            @Nullable Character identifierChar,
            String start,
            String expression,
            List<Parameter> parameters,
            String end,
            PlaceholderResolver parent,
            int relativeStart,
            int relativeStop
    ) {
        this.start = start;
        this.end = end;
        this.parent = parent;
        this.relativeStart = relativeStart;
        this.relativeStop = relativeStop;
        this.expression = expression;
        this.parameters = parameters;
        this.identifierChar = identifierChar;
    }

    /**
     * Returns the identifier character for this placeholder, if present.
     * <p>
     * The identifier character is the character that precedes the start delimiter,
     * such as '$' in "${expression}".
     *
     * @return an Optional containing the identifier character, or empty if there is none
     */
    public Optional<Character> getIdentifierChar() {
        return Optional.ofNullable(identifierChar);
    }

    /**
     * Returns the list of parameters associated with this placeholder.
     * <p>
     * Parameters are additional values that can be provided after the expression,
     * separated by delimiters.
     *
     * @return the list of parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Returns the start delimiter of this placeholder.
     * <p>
     * For example, in "${expression}", the start delimiter is "{".
     *
     * @return the start delimiter string
     */
    public String getStart() {
        return start;
    }

    /**
     * Returns the expression part of this placeholder.
     * <p>
     * This is the main content between the start and end delimiters,
     * excluding any parameters.
     *
     * @return the expression string
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Returns the end delimiter of this placeholder.
     * <p>
     * For example, in "${expression}", the end delimiter is "}".
     *
     * @return the end delimiter string
     */
    public String getEnd() {
        return end;
    }

    /**
     * Returns the PlaceholderResolver that created this placeholder.
     *
     * @return the parent PlaceholderResolver
     */
    public PlaceholderResolver getParent() {
        return parent;
    }

    /**
     * Returns the start position of this placeholder in the original string.
     *
     * @return the relative start position
     */
    public int getRelativeStart() {
        return relativeStart;
    }

    /**
     * Returns the end position of this placeholder in the original string.
     *
     * @return the relative stop position
     */
    public int getRelativeStop() {
        return relativeStop;
    }

    /**
     * Compiles this placeholder back into its string representation.
     * <p>
     * This method reconstructs the complete placeholder string, including the
     * identifier character, start delimiter, expression, parameters, and end delimiter.
     *
     * @return the compiled placeholder string
     */
    public String compile() {
        StringBuilder stringBuilder = new StringBuilder();
        if (identifierChar != null) {
            stringBuilder.append(identifierChar);
        }

        stringBuilder.append(start).append(expression);

        parameters.forEach(parameter -> stringBuilder.append(parameter.delimiter).append(parameter.content));
        return stringBuilder.append(end).toString();
    }

    /**
     * Replaces this placeholder in the given string with the replacement value,
     * using the relative positions stored in this placeholder.
     * <p>
     * This method is more efficient than {@link #replaceIn(String, String)} when
     * the relative positions are known and valid.
     *
     * @param wholeString the string containing this placeholder
     * @param replacement the value to replace this placeholder with
     * @return the string with this placeholder replaced
     */
    public String replaceRelativeIn(String wholeString, String replacement) {
        String start = wholeString.substring(0, relativeStart);
        String stop = wholeString.substring(relativeStop);
        return start + replacement + stop;
    }

    /**
     * Replaces all occurrences of this placeholder in the given string with the replacement value.
     * <p>
     * This method compiles the placeholder to its string representation and then
     * replaces all occurrences of that string in the input.
     *
     * @param wholeString the string containing this placeholder
     * @param replacement the value to replace this placeholder with
     * @return the string with all occurrences of this placeholder replaced
     */
    public String replaceIn(String wholeString, String replacement) {
        return wholeString.replace(compile(), replacement);
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
                && Objects.equals(expression, that.expression)
                && Objects.equals(end, that.end)
                && Objects.equals(parameters, that.parameters)
                && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifierChar, start, expression, end, parameters, parent, relativeStart, relativeStop);
    }

    @Override
    public String toString() {
        return compile();
    }

    /**
     * A default value of the expression.
     * <p>
     * A default value is present in the expression, like this: {@code <char>{<expression>:<defaultValue>} }.
     * The {@link #content} is equivalent to {@code <defaultValue>}, whilst {@link #delimiter} is {@code :}.
     * <p>
     * The delimiter can be changed when resolving default values, and it can be used during processing.
     * <p>
     * The default value itself can again be an expression.
     * To resolve it, you can use {@link #asPlaceholder()}, which will try to resolve the value from the parent.
     *
     * @param content   the content of the default value
     * @param delimiter the delimiter used for separating the expression from the default value
     * @param parent    the resolver that resolved the Placeholder plus Default combination.
     */
    public record Parameter(
            String content,
            String delimiter,
            PlaceholderResolver parent
    ) {
        @Override
        public String toString() {
            return delimiter + content;
        }

        /**
         * Resolves the {@link #content} from the {@link #parent}.
         * <p>
         * The default value can again be an expression, for example like this:
         * <p>
         * {@code ${expression:${nested-expression:nested-default}}}.
         * <p>
         * The default value of the expression will be {@code ${nested-expression:nested-default}}.
         * This method will evaluate the default value.
         *
         * @return a new Placeholder, if any.
         */
        public Optional<Placeholder> asPlaceholder() {
            List<Placeholder> placeholders = parent.resolveAllIn(content);

            if (placeholders.size() == 1) {
                return Optional.ofNullable(placeholders.getFirst());
            }

            return Optional.empty();
        }
    }
}
