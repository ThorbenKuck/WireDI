package com.wiredi.processor.integration;

import com.wiredi.compiler.processor.lang.WireDiRootAnnotationProcessor;
import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.files.utils.JavaFileObjectFactory;
import com.wiredi.compiler.tests.junit.CompilerSetup;
import com.wiredi.compiler.tests.junit.CompilerTest;
import com.wiredi.compiler.tests.result.Compilation;

import static com.wiredi.compiler.tests.Assertions.assertThat;

@CompilerSetup(processors = WireDiRootAnnotationProcessor.class, rootFolder = "com.wiredi.qualifier")
public class QualifierWireProcessorTest extends AbstractProcessorTest {

    @CompilerTest(classes = "TestClass")
    public void test(
            Compilation compilation,
            FileManagerState files,
            JavaFileObjectFactory javaFiles
    ) {
        assertThat(compilation)
                .wasSuccessful()
                .hasNoErrors()
                .hasNoWarnings();
        assertThat(files)
                .containsExactlyAllGeneratedFiles(
                        javaFiles.load("TestClassIdentifiableProvider")
                );
    }
}
