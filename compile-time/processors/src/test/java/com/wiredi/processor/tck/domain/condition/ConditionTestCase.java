package com.wiredi.processor.tck.domain.condition;

import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class ConditionTestCase implements TckTestCase {

    private final ConditionalClass conditionalClass;

    public ConditionTestCase(ConditionalClass conditionalClass) {
        this.conditionalClass = conditionalClass;
    }

    @Override
    public Collection<DynamicNode> dynamicTests() {
        return List.of(
                dynamicTest("ConditionalOnMissingBean is correctly evaluated in order", () -> assertThat(conditionalClass).isInstanceOf(ShouldBeTaken.class))
        );
    }
}
