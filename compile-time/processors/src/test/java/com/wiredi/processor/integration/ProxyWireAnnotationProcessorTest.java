package com.wiredi.processor.integration;

//
//import static com.wiredi.compiler.tests.Assertions.assertThat;
//
//@CompilerSetup(rootFolder = "com.wiredi.aop", processors = WireDiCompositeAnnotationProcessor.class)
//public class ProxyWireAnnotationProcessorTest extends AbstractProcessorTest {
//
//    @CompilerTest(classes = {"Transactional", "ProxyTarget", "TransactionalHandler"})
//    public void verifyThatTheProxiesAreGeneratedSuccessfully(
//            Compilation compilation,
//            FileManagerState state,
//            JavaFileObjectFactory javaFiles
//    ) {
//        assertThat(compilation)
//                .wasSuccessful()
//                .hasNoErrors()
//                .hasNoWarnings();
//
////        assertThat(state)
////                .containsGeneratedFile(javaFiles.load("ProxyTarget$$AspectAwareProxy"));
//        assertThat(state)
//                .containsGeneratedFile(javaFiles.load("TransactionalHandler$handle$AspectHandlerIdentifiableProvider"));
//        assertThat(state)
//                .containsGeneratedFile(javaFiles.load("ProxyTarget$$AspectAwareProxyIdentifiableProvider"));
//        assertThat(state)
//                .containsExactlyAllGeneratedFiles(
//                        javaFiles.load("ProxyTarget$$AspectAwareProxy"),
//                        javaFiles.load("TransactionalHandler$handle$AspectHandlerIdentifiableProvider"),
//                        javaFiles.load("TransactionalHandlerIdentifiableProvider"),
//                        javaFiles.load("TransactionalHandler$handle$AspectHandler"),
//                        javaFiles.load("ProxyTarget$$AspectAwareProxyIdentifiableProvider")
//                );
//
//    }
//}

public class ProxyWireAnnotationProcessorTest {
}
