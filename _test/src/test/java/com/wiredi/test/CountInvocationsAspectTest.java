package com.wiredi.test;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.WiredApplicationInstance;
import com.wiredi.test.inner.SuperDi;
import com.wiredi.tests.ApplicationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ApplicationTest
class CountInvocationsAspectTest {

    private static final Logging logger = Logging.getInstance(CountInvocationsAspectTest.class);

    @Test
    public void testThatSimpleInvocationsAreIntercepted(
            CountInvocationsAspect aspect,
            SuperDi superDi
    ) {
        // Arrange
        aspect.reset();
        assertThat(aspect.invocations())
                .withFailMessage("Precondition failed, CountInvocations was not zero")
                .isEqualTo(0);

        // Act
        superDi.countMe();

        // Assert
        assertThat(aspect.invocations()).isEqualTo(1);
    }

    @Test
    public void testThatPrivateMethodInvocationsAreInterceptedTwice(
            CountInvocationsAspect aspect,
            SuperDi superDi
    ) {
        // Arrange
        aspect.reset();
        assertThat(aspect.invocations())
                .withFailMessage("Precondition failed, CountInvocations was not zero")
                .isEqualTo(0);

        // Act
        superDi.countMeAndInvokeOtherCountMe();

        // Assert
        logger.debug("Counter " + this);
        assertThat(aspect.invocations()).isEqualTo(2);
    }

}