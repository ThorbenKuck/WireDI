package com.wiredi.processor.integration;

import com.wiredi.compiler.tests.files.utils.JavaFileObjectFactory;
import com.wiredi.compiler.tests.junit.CompilerSetup;
import com.wiredi.compiler.tests.junit.CompilerTest;
import com.wiredi.compiler.tests.result.Compilation;
import com.wiredi.compiler.processors.WireProcessor;

import static com.wiredi.compiler.tests.Assertions.assertThat;

@CompilerSetup(processors = WireProcessor.class, rootFolder = "com.wiredi.order")
class OrderWireProcessorTest extends AbstractProcessorTest {

    @CompilerTest(classes = {"FirstClass", "SecondClass"})
    public void testThatOrderBeforeCorrectResolved(
            Compilation compilation,
            JavaFileObjectFactory factory
    ) {
        assertThat(compilation)
                .wasSuccessful()
                .hasNoErrors()
                .hasNoWarnings();
        assertThat(compilation.files())
                .generatedSources()
                .containExactly(
                        factory.load("FirstClassIdentifiableProvider"),
                        factory.load("SecondClassIdentifiableProvider")
                );
    }
}
