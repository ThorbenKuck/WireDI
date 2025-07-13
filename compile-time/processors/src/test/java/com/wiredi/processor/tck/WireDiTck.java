package com.wiredi.processor.tck;

import com.wiredi.processor.tck.domain.InjectionTest;
import com.wiredi.processor.tck.domain.condition.ConditionTestCase;
import com.wiredi.processor.tck.domain.example.Car;
import com.wiredi.processor.tck.domain.example.V1Engine;
import com.wiredi.processor.tck.domain.generics.GenericTestCase;
import com.wiredi.processor.tck.domain.ordered.CommandBasedStringBuilder;
import com.wiredi.processor.tck.domain.override.OverwritingTestClass;
import com.wiredi.processor.tck.domain.override.OverwrittenTestClass;
import com.wiredi.processor.tck.domain.provide.CoffeeMachine;
import com.wiredi.processor.tck.domain.scopes.Scopes;
import com.wiredi.processor.tck.domain.transactional.TransactionalTestController;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.TimedValue;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

public class WireDiTck {

    @Test
    public void test() {
        // Arrange
        Environment environment = Environment.build();
//        WireContainer wireRepository = WireContainer.open(new BeanContainerProperties(environment).withConflictResolver(StandardWireConflictResolver.BEST_MATCH));
        WireContainer wireRepository = WireContainer.open(environment);

        // Act
        Car car = wireRepository.get(Car.class);

        // Assert
        if (!(car.getEngine() instanceof V1Engine)) {
            fail("Engine is not V1Engine, but " + car.getEngine().getClass().getSimpleName());
        }
    }

    @Test
    public void testLoadTimeOfObjects() {
        WireContainer wireRepository = WireContainer.create();
        TimedValue<Collection<TckTestCase>> timedValue = Timed.of(() -> wireRepository.getAll(TypeIdentifier.of(TckTestCase.class)));

        System.out.println(timedValue.time());
        assertThat(timedValue.time().get(TimeUnit.MILLISECONDS)).isLessThan(100);
    }

    @TestFactory
    public Collection<DynamicNode> verifyScopesWork() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(Scopes.class))
                .withFailMessage("The Qualifications was not wired correctly")
                .isPresent();

        return wireRepository.get(Scopes.class).dynamicTests();
    }

    @TestFactory
    public Collection<DynamicNode> verifyThatTheAspectProxiesWork() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(TransactionalTestController.class))
                .withFailMessage("The TransactionalTestController was not wired correctly")
                .isPresent();

        return wireRepository.get(TransactionalTestController.class).dynamicTests();
    }

    @TestFactory
    public Collection<DynamicNode> verifyThatOrderingWorksCorrectly() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(CommandBasedStringBuilder.class))
                .withFailMessage("The CommandBasedStringBuilder was not wired correctly")
                .isPresent();

        return wireRepository.get(CommandBasedStringBuilder.class).dynamicTests();
    }

    @TestFactory
    public Collection<DynamicNode> verifyThatProducersWithQualifiersWork() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(CoffeeMachine.class))
                .withFailMessage("The Coffee Machine was not wired correctly")
                .isPresent();

        return wireRepository.get(CoffeeMachine.class).dynamicTests();
    }

    @TestFactory
    public Collection<DynamicNode> verifyThatSimpleInjectionWorks() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(InjectionTest.class))
                .withFailMessage("The InjectionTest was not wired correctly")
                .isPresent();

        return wireRepository.get(InjectionTest.class).dynamicTests();
    }

    @TestFactory
    public Collection<DynamicNode> verifyThatConditionEvaluationWorks() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(ConditionTestCase.class))
                .withFailMessage("The InjectionTest was not wired correctly")
                .isPresent();

        return wireRepository.get(ConditionTestCase.class).dynamicTests();
    }

    @TestFactory
    public Collection<DynamicNode> overwritingBehaviourTest() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(OverwrittenTestClass.class))
                .withFailMessage("The OverwrittenTestClass was not wired correctly")
                .isPresent();

        return wireRepository.get(OverwritingTestClass.class).dynamicTests();
    }

    @TestFactory
    public Collection<DynamicNode> genericBehaviourTest() {
        WireContainer wireRepository = WireContainer.open();
        assertThat(wireRepository.tryGet(GenericTestCase.class))
                .withFailMessage("The OverwrittenTestClass was not wired correctly")
                .isPresent();

        return wireRepository.get(GenericTestCase.class).dynamicTests();
    }

    @TestFactory
    public Collection<? extends DynamicNode> repeatingDynamicTestCases() {
        return IntStream.range(0, 20)
                .mapToObj(round -> {
                    WireContainer wireRepository = WireContainer.open();
                    Collection<TckTestCase> tckTestCases = wireRepository.getAll(TypeIdentifier.of(TckTestCase.class));

                    return dynamicContainer("Repetition-" + round, () -> tckTestCases.stream()
                            .map(testCase -> dynamicContainer(testCase.getClass().getSimpleName(), () -> testCase.dynamicTests().iterator()))
                            .map(it -> (DynamicNode) it).toList().iterator()
                    );
                }).toList();
    }
}
