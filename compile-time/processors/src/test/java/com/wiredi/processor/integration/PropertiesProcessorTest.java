package com.wiredi.processor.integration;

import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.junit.CompilerSetup;
import com.wiredi.compiler.tests.junit.CompilerTest;
import com.wiredi.compiler.tests.result.Compilation;
import com.wiredi.compiler.processors.PropertyWireProcessor;

import static com.wiredi.compiler.tests.Assertions.assertThat;

@CompilerSetup(processors = PropertyWireProcessor.class, rootFolder = "com.wiredi.properties")
public class PropertiesProcessorTest extends AbstractProcessorTest {

    @CompilerTest(classes = "PropertyBindingExample")
    public void verifyThatInheritedWireAnnotationWork(Compilation compilation, FileManagerState state) {
        assertThat(compilation)
                .wasSuccessful()
                .hasNoErrors()
                .hasNoWarnings();

        assertThat(state)
                .containsGeneratedFile("com.wiredi.properties.PropertyBindingExampleIdentifiableProvider");
    }
}
