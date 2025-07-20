package com.wiredi.runtime;

import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.lang.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ConditionEvaluationReporterTest {

    @Mock
    private ProviderCatalog mockProviderCatalog;

    @Mock
    private ConditionEvaluation mockConditionEvaluation;

    @Mock
    private WireContainer mockWireContainer;

    @Mock
    private IdentifiableProvider<?> mockProvider;

    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private ConditionEvaluationReporter.PrintStreamReporter reporter;
    private ConditionEvaluationContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
        reporter = new ConditionEvaluationReporter.PrintStreamReporter(printStream);

        Counter appliedCounter = new Counter();
        Counter roundsCounter = new Counter();
        Integer threshold = 5;

        List<IdentifiableProvider<?>> providers = new ArrayList<>();
        providers.add(mockProvider);
        doReturn(providers).when(mockProviderCatalog).conditionalProviders();
        when(mockProvider.toString()).thenReturn("TestProvider");

        context = new ConditionEvaluationContext(
                mockProviderCatalog,
                appliedCounter,
                roundsCounter,
                threshold,
                mockConditionEvaluation,
                mockWireContainer
        );
    }

    @Test
    void report_shouldPrintBasicInformation() {
        // Arrange
        // No need to mock forEach since we're not testing the iteration behavior

        // Act
        reporter.report(context);
        String output = outputStream.toString();

        // Assert
        assertThat(output).contains("Condition Evaluation:");
        assertThat(output).contains("=====================");
        assertThat(output).contains("Providers         : 1");
        assertThat(output).contains("Applied           : 0");
        assertThat(output).contains("Condition Rounds  : 0");
        assertThat(output).contains("Round Threshold   : 5");
    }

    @Test
    void report_shouldPrintPositiveMatches() {
        // Arrange
        Set<String> positiveMatches = new HashSet<>();
        positiveMatches.add("Match1");
        positiveMatches.add("Match2");
        Set<String> negativeMatches = new HashSet<>();

        setupMockEvaluation(positiveMatches, negativeMatches);

        // Act
        reporter.report(context);
        String output = outputStream.toString();

        // Assert
        assertThat(output).contains("# TestProvider");
        assertThat(output).contains("++ Matched ++");
        assertThat(output).contains("- Match1");
        assertThat(output).contains("- Match2");
        assertThat(output).doesNotContain("-- Not Matched --");
    }

    @Test
    void report_shouldPrintNegativeMatches() {
        // Arrange
        Set<String> positiveMatches = new HashSet<>();
        Set<String> negativeMatches = new HashSet<>();
        negativeMatches.add("NoMatch1");
        negativeMatches.add("NoMatch2");

        setupMockEvaluation(positiveMatches, negativeMatches);

        // Act
        reporter.report(context);
        String output = outputStream.toString();

        // Assert
        assertThat(output).contains("# TestProvider");
        assertThat(output).contains("-- Not Matched --");
        assertThat(output).contains("- NoMatch1");
        assertThat(output).contains("- NoMatch2");
        assertThat(output).doesNotContain("++ Matched ++");
    }

    @Test
    void report_shouldPrintBothPositiveAndNegativeMatches() {
        // Arrange
        Set<String> positiveMatches = new HashSet<>();
        positiveMatches.add("Match1");
        Set<String> negativeMatches = new HashSet<>();
        negativeMatches.add("NoMatch1");

        setupMockEvaluation(positiveMatches, negativeMatches);

        // Act
        reporter.report(context);
        String output = outputStream.toString();

        // Assert
        assertThat(output).contains("# TestProvider");
        assertThat(output).contains("++ Matched ++");
        assertThat(output).contains("- Match1");
        assertThat(output).contains("-- Not Matched --");
        assertThat(output).contains("- NoMatch1");
    }

    private void setupMockEvaluation(Set<String> positiveMatches, Set<String> negativeMatches) {
        ConditionEvaluation.Context evaluationContext = mock(ConditionEvaluation.Context.class);
        doReturn(mockProvider).when(evaluationContext).provider();
        doReturn(positiveMatches).when(evaluationContext).positiveMatches();
        doReturn(negativeMatches).when(evaluationContext).negativeMatches();

        // Mock the forEach method to execute the consumer with our mock context
        doAnswer(invocation -> {
            java.util.function.Consumer<ConditionEvaluation.Context> consumer = invocation.getArgument(0);
            consumer.accept(evaluationContext);
            return null;
        }).when(mockConditionEvaluation).forEach(any());
    }
}
