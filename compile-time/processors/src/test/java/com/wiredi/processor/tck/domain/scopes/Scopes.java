package com.wiredi.processor.tck.domain.scopes;

import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.domain.provide.coffee.Coffee;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import com.wiredi.runtime.WireContainer;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class Scopes implements TckTestCase {

    private final WireContainer container;

    public Scopes(WireContainer container) {
        this.container = container;
    }

    @Override
    public Collection<DynamicNode> dynamicTests() {
        return List.of(
                dynamicTest("Prototype beans are instantiated every time", () -> assertNotSame(container.get(BeanPrototype.class), container.get(BeanPrototype.class))),
                dynamicTest("Singleton beans are instantiated only once", () -> assertSame(container.get(BeanSingleton.class), container.get(BeanSingleton.class))),
                dynamicTest("Requesting all beans should still return both", () -> assertThat(container.getAll(Coffee.class)).hasSize(2))
        );
    }
}
