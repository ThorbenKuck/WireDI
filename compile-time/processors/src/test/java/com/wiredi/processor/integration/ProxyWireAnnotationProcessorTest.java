package com.wiredi.processor.integration;

import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.files.utils.JavaFileObjectFactory;
import com.wiredi.compiler.tests.junit.CompilerTest;
import com.wiredi.compiler.tests.junit.CompilerSetup;
import com.wiredi.compiler.tests.result.Compilation;
import com.wiredi.compiler.processors.WireProcessor;

import static com.wiredi.compiler.tests.Assertions.assertThat;

@CompilerSetup(rootFolder = "com.wiredi.aop", processors = WireProcessor.class)
public class ProxyWireAnnotationProcessorTest extends AbstractProcessorTest {

    @CompilerTest(classes = {"Transactional", "ProxyTarget", "TransactionalHandler"})
    public void verifyThatTheProxiesAreGeneratedSuccessfully(
            Compilation compilation,
            FileManagerState state,
            JavaFileObjectFactory javaFiles
    ) {
        assertThat(compilation)
                .wasSuccessful()
                .hasNoErrors()
                .hasNoWarnings();

//        assertThat(state)
//                .containsGeneratedFile(javaFiles.load("ProxyTarget$$AspectAwareProxy"));
        assertThat(state)
                .containsGeneratedFile(javaFiles.load("TransactionalHandler$handle$AspectHandlerIdentifiableProvider"));
        assertThat(state)
                .containsGeneratedFile(javaFiles.load("ProxyTarget$$AspectAwareProxyIdentifiableProvider"));
        assertThat(state)
                .containsExactlyAllGeneratedFiles(
                        javaFiles.load("ProxyTarget$$AspectAwareProxy"),
                        javaFiles.load("TransactionalHandler$handle$AspectHandlerIdentifiableProvider"),
                        javaFiles.load("TransactionalHandlerIdentifiableProvider"),
                        javaFiles.load("TransactionalHandler$handle$AspectHandler"),
                        javaFiles.load("ProxyTarget$$AspectAwareProxyIdentifiableProvider")
                );

    }
}
