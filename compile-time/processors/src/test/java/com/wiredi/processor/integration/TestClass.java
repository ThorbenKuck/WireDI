package com.wiredi.processor.integration;

import org.junit.jupiter.api.Nested;



public class TestClass {

    private final InnerTestClass innerTestClass;

    public TestClass(InnerTestClass innerTestClass) {
        this.innerTestClass = innerTestClass;
    }

    public class InnerTestClass {
        private final String string;

        public InnerTestClass(String string) {
            this.string = string;
        }
    }
}
