package com.wiredi.processor.integration;

import com.wiredi.compiler.processor.lang.WireDiRootAnnotationProcessor;
import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.junit.CompilerTest;
import com.wiredi.compiler.tests.junit.CompilerSetup;
import com.wiredi.compiler.tests.result.Compilation;

import static com.wiredi.compiler.tests.Assertions.assertThat;

@CompilerSetup(processors = WireDiRootAnnotationProcessor.class, rootFolder = "com.wiredi.inherited")
@CompilerTest(classes = "ExampleConfiguration")
public class InheritedAnnotationWireProcessorTest extends AbstractProcessorTest {

    @CompilerTest(classes = "Input")
    public void verifyThatInheritedWireAnnotationWork(Compilation compilation, FileManagerState state) {
        assertThat(compilation)
                .wasSuccessful()
                .hasNoErrors()
                .hasNoWarnings();
        assertThat(state)
                .containsGeneratedFile("com.wiredi.inherited.InputIdentifiableProvider");
    }

    @CompilerTest(classes = "CustomAutoConfiguration")
    public void verifyThatAutoConfigurationInheritsOrder(Compilation compilation, FileManagerState state) {
        assertThat(compilation)
                .wasSuccessful()
                .hasNoErrors()
                .hasNoWarnings();
        assertThat(state)
                .containsGeneratedFile("com.wiredi.inherited.CustomAutoConfigurationIdentifiableProvider");
    }
}
