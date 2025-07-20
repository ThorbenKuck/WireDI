package com.wiredi.runtime;

import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.lang.Counter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionEvaluationContextTest {

    @Test
    void testRecordAccessors() {
        // Arrange
        ProviderCatalog providerCatalog = Mockito.mock(ProviderCatalog.class);
        Counter appliedCounter = new Counter();
        Counter roundsCounter = new Counter();
        Integer threshold = 5;
        ConditionEvaluation conditionEvaluation = Mockito.mock(ConditionEvaluation.class);
        WireContainer wireContainer = Mockito.mock(WireContainer.class);

        // Act
        ConditionEvaluationContext context = new ConditionEvaluationContext(
                providerCatalog,
                appliedCounter,
                roundsCounter,
                threshold,
                conditionEvaluation,
                wireContainer
        );

        // Assert
        assertThat(context.providerCatalog()).isSameAs(providerCatalog);
        assertThat(context.appliedConditionalProviders()).isSameAs(appliedCounter);
        assertThat(context.additionalRounds()).isSameAs(roundsCounter);
        assertThat(context.conditionalRoundThreshold()).isEqualTo(threshold);
        assertThat(context.conditionEvaluation()).isSameAs(conditionEvaluation);
        assertThat(context.wireContainer()).isSameAs(wireContainer);
    }
}