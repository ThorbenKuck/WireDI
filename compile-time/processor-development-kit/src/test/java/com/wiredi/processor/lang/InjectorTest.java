package com.wiredi.processor.lang;

import com.wiredi.compiler.Injector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class InjectorTest {

    @Test
    public void testInjection() {
        Injector injector = new Injector();

        TestClass testClass = injector.get(TestClass.class);
        assertNotNull(testClass.getLeaf2(), "Direct dependency is not null");
        assertNotNull(testClass.getLeaf(), "Parent dependency is not null");
    }

    @Test
    @Disabled // TODO: Find out why this faily
    public void testThatAnExampleProcessorCanBeWired() {
        ExampleProcessor wireDiBaseProcessor = new ExampleProcessor();
        wireDiBaseProcessor.init(new ProcessingEnvironmentMock());

        assertThat(wireDiBaseProcessor.logger).isNotNull();
        assertThat(wireDiBaseProcessor.compilerRepository).isNotNull();
        assertThat(wireDiBaseProcessor.types).isNotNull();
        assertThat(wireDiBaseProcessor.injectionPointService).isNotNull();
        assertThat(wireDiBaseProcessor.proxyService).isNotNull();
    }
}

class Leaf {}
class Leaf2 {}

abstract class AbstractClass {

    @Inject
    private Leaf leaf;

    public Leaf getLeaf() {
        return leaf;
    }
}

class TestClass extends AbstractClass {

    @Inject
    private Leaf2 leaf;

    public Leaf2 getLeaf2() {
        return leaf;
    }
}