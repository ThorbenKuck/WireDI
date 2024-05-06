package com.wiredi.compiler.tests;

import com.wiredi.compiler.tests.result.Compilation;
import org.junit.jupiter.api.Test;

import static com.wiredi.compiler.tests.Assertions.assertThat;

class CompilerSetup {

    @Test
    public void test() {
        Compilation compilation = Compiler.javac()
                .withCurrentClasspath()
                .withClass("TestClass")
                .compile();

        assertThat(compilation).hasNoErrors();
    }
}
