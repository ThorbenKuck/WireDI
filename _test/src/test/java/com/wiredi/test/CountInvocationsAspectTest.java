package com.wiredi.test;

import com.wiredi.runtime.WireContainer;
import com.wiredi.test.inner.SuperDi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CountInvocationsAspectTest extends AbstractIntegrationTest {

    @Test
    public void testThatSimpleInvocationsAreIntercepted() {
        // Arrange
        WireContainer wireContainer = loadWireRepository();
        CountInvocationsAspect aspect = wireContainer.get(CountInvocationsAspect.class);
        assertThat(aspect.invocations())
                .withFailMessage("Precondition failed, CountInvocations was not zero")
                .isEqualTo(0);

        // Act
        wireContainer.get(SuperDi.class).countMe();

        // Assert
        assertThat(aspect.invocations()).isEqualTo(1);
    }

    @Test
    public void testThatPrivateMethodInvocationsAreInterceptedTwice() {
        // Arrange
        WireContainer wireContainer = loadWireRepository();
        CountInvocationsAspect aspect = wireContainer.get(CountInvocationsAspect.class);
        assertThat(aspect.invocations())
                .withFailMessage("Precondition failed, CountInvocations was not zero")
                .isEqualTo(0);

        // Act
        wireContainer.get(SuperDi.class).countMeAndInvokeOtherCountMe();

        // Assert
        assertThat(aspect.invocations()).isEqualTo(2);
    }

}