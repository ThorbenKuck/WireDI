package com.wiredi.processor.integration;

import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.files.utils.JavaFileObjectFactory;
import com.wiredi.compiler.tests.junit.CompilerTest;
import com.wiredi.compiler.tests.junit.CompilerSetup;
import com.wiredi.compiler.tests.result.Compilation;
import com.wiredi.compiler.processors.WireProcessor;

import static com.wiredi.compiler.tests.Assertions.assertThat;

@CompilerSetup(processors = WireProcessor.class, rootFolder = "com.wiredi.provider")
public class ProviderWireProcessorTest extends AbstractProcessorTest {
    @CompilerTest(classes = {"Configuration", "Implementation", "Interface"})
    public void testThatTheConfigurationWorks(
            Compilation compilation,
            FileManagerState fileManagerState,
            JavaFileObjectFactory factory
    ) {
        assertThat(compilation)
                .wasSuccessful()
                .hasNoErrors()
                .hasNoWarnings();

        assertThat(fileManagerState).containsExactlyAllGeneratedFiles(
                factory.load("InterfaceProvider$implementation$Configuration"),
                factory.load("ConfigurationIdentifiableProvider")
        );
    }
}
