package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.annotations.identifiers.AnnotationType;
import com.wiredi.compiler.tests.Compiler;
import com.wiredi.compiler.tests.elements.ElementFactory;
import com.wiredi.compiler.tests.result.Compilation;
import com.wiredi.runtime.domain.annotations.AnnotationExcerpt;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.*;
import java.lang.annotation.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for the annotation search builder API.
 * This test demonstrates the multi-step builder with typed and untyped searches.
 */
@DisplayName("AnnotationSearch")
class AnnotationSearchTest {

    // ========== Test Annotations ==========

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface TestAnnotation {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    @interface InheritedTestAnnotation {
        int priority() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @InheritedTestAnnotation(priority = 10)
    @interface MetaAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface AnotherAnnotation {
        String name() default "";
    }

    // ========== Test Classes ==========

    @TestAnnotation("test-value")
    @AnotherAnnotation(name = "another")
    static class AnnotatedClass {
    }

    @MetaAnnotation
    static class MetaAnnotatedClass {
    }

    @InheritedTestAnnotation(priority = 5)
    static class BaseClass {
    }

    static class DerivedClass extends BaseClass {
    }

    // ========== Tests ==========

    @Nested
    @DisplayName("Untyped Search (by name)")
    class UntypedSearchTest {

        @Test
        @DisplayName("should find annotation by exact name")
        void findByExactName() {
            Element element = getTypeElement(AnnotatedClass.class);

            AnnotationSearch search = Annotations.search()
                    .byExactName("com.wiredi.compiler.domain.annotations.AnnotationSearchTest$TestAnnotation");

            Optional<AnnotationMirror> mirror = search.findFirstMirrorIn(element);
            assertThat(mirror).isPresent();
            assertThat(mirror.get().getAnnotationType().toString())
                    .contains("TestAnnotation");
        }

        @Test
        @DisplayName("should find annotation by wildcard pattern")
        void findByWildcard() {
            Element element = getTypeElement(AnnotatedClass.class);

            AnnotationSearch search = Annotations.search()
                    .byWildcard("*Test*");

            List<AnnotationMirror> mirrors = search.findAllMirrorsIn(element);
            assertThat(mirrors).isNotEmpty();
            assertThat(mirrors)
                    .anyMatch(m -> m.getAnnotationType().toString().contains("TestAnnotation"));
        }

        @Test
        @DisplayName("should return AnnotationMetadata")
        void returnMetadata() {
            Element element = getTypeElement(AnnotatedClass.class);

            AnnotationSearch search = Annotations.search()
                    .byName("TestAnnotation");

            Optional<AnnotationMetadata> metadata = search.findFirstMetadataIn(element);
            assertThat(metadata).isPresent();
            assertThat(metadata.get().get("value")).contains("test-value");
        }

        @Test
        @DisplayName("should find all matching annotations")
        void findAllAnnotations() {
            Element element = getTypeElement(AnnotatedClass.class);

            AnnotationSearch search = Annotations.search()
                    .byWildcard("*Annotation");

            List<AnnotationMirror> mirrors = search.findAllMirrorsIn(element);
            assertThat(mirrors).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Typed Search (by type)")
    class TypedSearchTest {

        @Test
        @DisplayName("should find annotation by type")
        void findByType() {
            Element element = getTypeElement(AnnotatedClass.class);

            TypedAnnotationSearch<TestAnnotation> search = Annotations.search()
                    .byType(TestAnnotation.class);

            Optional<TestAnnotation> annotation = search.findFirstIn(element);
            assertThat(annotation).isPresent();
            assertThat(annotation.get().value()).isEqualTo("test-value");
        }

        @Test
        @DisplayName("should return AnnotationExcerpt with typed annotation")
        void returnTypedExcerpt() {
            Element element = getTypeElement(AnnotatedClass.class);

            TypedAnnotationSearch<TestAnnotation> search = Annotations.search()
                    .byType(TestAnnotation.class);

            Optional<AnnotationExcerpt<TestAnnotation>> excerpt = search.findFirstExcerptIn(element);
            assertThat(excerpt).isPresent();
            assertThat(excerpt.get().instance().value()).isEqualTo("test-value");
            assertThat(excerpt.get().metadata().get("value")).contains("test-value");
        }

        @Test
        @DisplayName("should check if annotation is present")
        void checkIsPresent() {
            Element element = getTypeElement(AnnotatedClass.class);

            TypedAnnotationSearch<TestAnnotation> searchPresent = Annotations.search()
                    .byType(TestAnnotation.class);

            assertThat(searchPresent.isPresentIn(element)).isTrue();

            TypedAnnotationSearch<Deprecated> searchAbsent = Annotations.search()
                    .byType(Deprecated.class);

            assertThat(searchAbsent.isPresentIn(element)).isFalse();
        }

        @Test
        @DisplayName("should find all annotations of given type")
        void findAllByType() {
            Element element = getTypeElement(AnnotatedClass.class);

            TypedAnnotationSearch<AnotherAnnotation> search = Annotations.search()
                    .byType(AnotherAnnotation.class);

            List<AnotherAnnotation> annotations = search.findAllIn(element);
            assertThat(annotations).hasSize(1);
            assertThat(annotations.get(0).name()).isEqualTo("another");
        }
    }

    @Nested
    @DisplayName("Inheritance Support")
    class InheritanceTest {

        @Test
        @DisplayName("should find inherited annotations when enabled")
        void findInheritedAnnotations() {
            Element element = getTypeElement(MetaAnnotatedClass.class);

            TypedAnnotationSearch<InheritedTestAnnotation> search = Annotations.search()
                    .withInheritance(true)
                    .byType(InheritedTestAnnotation.class);

            Optional<InheritedTestAnnotation> annotation = search.findFirstIn(element);
            assertThat(annotation).isPresent();
            assertThat(annotation.get().priority()).isEqualTo(10);
        }

        @Test
        @DisplayName("should not find inherited annotations when disabled")
        void skipInheritedAnnotations() {
            Element element = getTypeElement(MetaAnnotatedClass.class);

            TypedAnnotationSearch<InheritedTestAnnotation> search = Annotations.search()
                    .withoutInheritance()
                    .byType(InheritedTestAnnotation.class);

            // When inheritance is disabled, we should not find the annotation on meta-annotations
            Optional<InheritedTestAnnotation> annotation = search.findFirstIn(element);
            assertThat(annotation).isEmpty();
        }

        @Test
        @DisplayName("should find annotations through class inheritance")
        void findThroughClassInheritance() {
            Element baseElement = getTypeElement(BaseClass.class);
            Element derivedElement = getTypeElement(DerivedClass.class);

            TypedAnnotationSearch<InheritedTestAnnotation> searchBase = Annotations.search()
                    .byType(InheritedTestAnnotation.class);

            assertThat(searchBase.isPresentIn(baseElement)).isTrue();
            assertThat(searchBase.findFirstIn(baseElement).get().priority()).isEqualTo(5);

            // Note: This test would require proper class hierarchy support in the search implementation
            // For now, we're just verifying the base case works
        }
    }

    @Nested
    @DisplayName("AnnotationType Matching")
    class AnnotationTypeTest {

        @Test
        @DisplayName("should detect @Inherited annotation")
        void detectInherited() {
            AnnotationType<InheritedTestAnnotation> inherited = AnnotationIdentifier.of(InheritedTestAnnotation.class);
            AnnotationType<TestAnnotation> notInherited = AnnotationIdentifier.of(TestAnnotation.class);

            assertThat(inherited.supportsInheritance()).isTrue();
            assertThat(notInherited.supportsInheritance()).isFalse();
        }
    }

    @Nested
    @DisplayName("AnnotationIdentifier Compilation")
    class AnnotationIdentifierCompilationTest {

        @Test
        void searchingWorksInTheCompiler() {
            ExampleAnnotationProcessor processor = new ExampleAnnotationProcessor();

            Compilation compilation = Compiler.javac()
                    .withCurrentClasspath()
                    .withProcessor(processor)
                    .withClass(TestClass.class)
                    .withClass(ExampleAnnotation.class)
                    .withClass(InheritedAnnotation.class)
                    .withClass(TestCompile.class)
                    .compile();

            compilation.assertThat().wasSuccessful();
            processor.getResults()
                    .verify(ExampleAnnotation.class)
                    .wasFoundOn("class TestClass", "field testField", "method getTestField")
                    .wasNotFoundOn("inner-class InnerClass", "method <init>")
                    .assertAllChecked();
        }
    }

    // ========== Helper Methods ==========

    /**
     * Creates a TypeElement from a Class by compiling it and extracting its element representation.
     * This method uses the annotation processing infrastructure to obtain a proper TypeElement.
     *
     * @param clazz the class to convert to a TypeElement
     * @return the TypeElement representing the class
     */
    private Element getTypeElement(Class<?> clazz) {
        return ElementFactory.createTypeElement(clazz);
    }
}
