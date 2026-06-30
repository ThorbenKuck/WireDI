package com.wiredi.processor.tck.domain.cyclic;

import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.exceptions.CyclicDependencyException;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class CyclicTest implements TckTestCase {

    private final WireContainer container;

    public CyclicTest(WireContainer container) {
        this.container = container;
    }

    @Override
    public Collection<DynamicNode> dynamicTests() {
        return List.of(
                dynamicTest("Cyclic dependency should be detected", () -> assertThatCode(() -> container.get(A.class)).isInstanceOf(CyclicDependencyException.class))
        );
    }
}
