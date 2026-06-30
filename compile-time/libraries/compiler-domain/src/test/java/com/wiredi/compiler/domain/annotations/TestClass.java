package com.wiredi.compiler.domain.annotations;

@TestCompile
@ExampleAnnotation
public class TestClass {

    @ExampleAnnotation
    private String testField;

    @ExampleAnnotation
    public String getTestField() {
        return testField;
    }

    public class InnerClass {
        @ExampleAnnotation
        private String testField;

        @ExampleAnnotation
        public String getTestField() {
            return testField;
        }
    }

}
