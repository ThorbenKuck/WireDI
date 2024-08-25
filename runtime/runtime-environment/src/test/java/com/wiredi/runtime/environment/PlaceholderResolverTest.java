package com.wiredi.runtime.environment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderResolverTest {

    private final PlaceholderResolver placeholderResolver = new PlaceholderResolver("{", "}");

    @Test
    public void testThatPlaceholdersCanBeExtracted() {
        // Arrange
        String wholeString = "${some.value:default}";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(1);
        assertThat(placeholders.getFirst().getParameters()).containsExactly(new Placeholder.Parameter("default", ":", placeholderResolver));
    }

    @Test
    public void testThatMultipleParametersAreSupported() {
        // Arrange
        String wholeString = "${some.value:default1:default2}";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(1);
        assertThat(placeholders.getFirst().getParameters())
                .containsExactly(
                        new Placeholder.Parameter("default1", ":", placeholderResolver),
                        new Placeholder.Parameter("default2", ":", placeholderResolver)
                );
    }

    @Test
    public void testThatPlaceholdersInDefaultValueAreSupported() {
        // Arrange
        String wholeString = "${some.value:${some.other.value}}";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(1);
        assertThat(placeholders.getFirst().getParameters()).containsExactly(new Placeholder.Parameter("${some.other.value}", ":", placeholderResolver));
    }

    @Test
    public void testThatDoubleNestedPlaceholdersInDefaultValueAreSupported() {
        // Arrange
        String wholeString = "${first.value:%{second.value:#{third.value}}}";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(1);
        Placeholder first = placeholders.getFirst();
        assertThat(first.getExpression()).isEqualTo("first.value");
        assertThat(first.getIdentifierChar()).hasValue('$');
        Placeholder.Parameter firstDefault = first.getParameters().getFirst();
        Placeholder secondValue = firstDefault.asPlaceholder().get();
        assertThat(secondValue.getExpression()).isEqualTo("second.value");
        assertThat(secondValue.getIdentifierChar()).hasValue('%');
        Placeholder.Parameter secondDefault = secondValue.getParameters().getFirst();
        Placeholder thirdValue = secondDefault.asPlaceholder().get();
        assertThat(thirdValue.getExpression()).isEqualTo("third.value");
        assertThat(thirdValue.getIdentifierChar()).hasValue('#');
        assertThat(thirdValue.getParameters()).isEmpty();
    }

    @Test
    public void testThatRawPlaceholdersAreExtractedCorrectly() {
        // Arrange
        String rawPlaceHolder = "${some.value:${some.other.value}}";
        String wholeString = "This Variable " + rawPlaceHolder + " Is Only For A Test {";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(1);
        assertThat(placeholders.getFirst().toString()).isEqualTo(rawPlaceHolder);
    }

    @Test
    public void testThatTwoRawPlaceholdersAreExtractedCorrectly() {
        // Arrange
        String rawPlaceHolder = "${some.value:${some.other.value}}";
        String wholeString = "}This Variable " + rawPlaceHolder + " Is {} Only " + rawPlaceHolder + "For A Test {";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(2);
        Placeholder firstEntry = placeholders.get(0);
        Placeholder secondEntry = placeholders.get(1);
        assertThat(firstEntry.getIdentifierChar()).hasValue('$');
        assertThat(firstEntry.toString()).isEqualTo(rawPlaceHolder);
        assertThat(secondEntry.toString()).isEqualTo(rawPlaceHolder);
    }

}