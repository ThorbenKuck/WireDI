package com.wiredi.environment;

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
    }

    @Test
    public void testThatPlaceholdersInDefaultValueAreSupported() {
        // Arrange
        String wholeString = "${some.value:${some.other.value}}";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(1);
        assertThat(placeholders.get(0).getDefaultValue()).isNotEmpty().hasValue(new Placeholder.Default("${some.other.value}", ":", placeholderResolver));
    }

    @Test
    public void testThatDoubleNestedPlaceholdersInDefaultValueAreSupported() {
        // Arrange
        String wholeString = "${first.value:%{second.value:#{third.value}}}";

        // Act
        List<Placeholder> placeholders = placeholderResolver.resolveAllIn(wholeString);

        // Assert
        assertThat(placeholders).hasSize(1);
        Placeholder first = placeholders.get(0);
        assertThat(first.getContent()).isEqualTo("first.value");
        assertThat(first.getIdentifierChar()).hasValue('$');
        Placeholder.Default firstDefault = first.getDefaultValue().get();
        Placeholder secondValue = firstDefault.asPlaceholder().get();
        assertThat(secondValue.getContent()).isEqualTo("second.value");
        assertThat(secondValue.getIdentifierChar()).hasValue('%');
        Placeholder.Default secondDefault = secondValue.getDefaultValue().get();
        Placeholder thirdValue = secondDefault.asPlaceholder().get();
        assertThat(thirdValue.getContent()).isEqualTo("third.value");
        assertThat(thirdValue.getIdentifierChar()).hasValue('#');
        assertThat(thirdValue.getDefaultValue()).isEmpty();
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
        assertThat(placeholders.get(0).toString()).isEqualTo(rawPlaceHolder);
    }

    @Test
    public void testThatTwoRawPlaceholdersAreExtractedCorrectly() {
        // Arrange
        String rawPlaceHolder = "${some.value:${some.other.value}}";
        String wholeString = "}This Variable " + rawPlaceHolder + " Is Only " + rawPlaceHolder + "For A Test {";

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