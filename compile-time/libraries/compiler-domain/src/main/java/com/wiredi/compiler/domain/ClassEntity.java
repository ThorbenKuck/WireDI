package com.wiredi.compiler.domain;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.entities.FieldFactory;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface ClassEntity<T extends ClassEntity<?>> {

    /**
     * Invalidates the current class entity.
     * <p>
     * An invalidated class entity will not be flushed out, and the resulting TypeSpec will not be written
     * as a class.
     * This is allowing plugins to prevent the creation of classes, if they want the processor to behave differently.
     * <p>
     * One example for this is the AspectAwareProxy. As we want to change the default wire behavior and wire the proxy
     * in place of the original class, we invalidate the initial ClassEntity.
     * We then create a different class entity and annotate it with @Wire, so that this is picked up later instead.
     */
    void invalidate();

    <A extends Annotation> List<Annotations.Result<A>> findAnnotations(Class<A> type);

    boolean isValid();

    Optional<PackageElement> packageElement();

    T setConstructor(MethodFactory methodFactory);

    T addInterface(TypeName typeName);

    default T setPackageOf(Element element) {
        return setPackage(TypeUtils.packageOf(element));
    }

    T setPackage(PackageElement packageElement);

    T addSource(Element element);

    ClassName compileFinalClassName();

    String className();

    boolean willHaveTheSamePackageAs(Element element);

    TypeSpec compile();

    TypeMirror rootType();

    // ######## Annotation modification ########

    T addAnnotation(AnnotationSpec annotationSpec);

    default T addAnnotation(Class<?> type) {
        return addAnnotation(AnnotationSpec.builder(type).build());
    }

    default T addAnnotation(ClassName className) {
        return addAnnotation(AnnotationSpec.builder(className).build());
    }

    // ######## Method modifications ########

    T addMethod(MethodSpec method);

    T addMethod(String name, MethodFactory methodFactory);

    default T addMethod(StandaloneMethodFactory methodFactory) {
        if (!methodFactory.applies(this)) {
            return (T) this;
        }

        return addMethod(methodFactory.methodName(), methodFactory);
    }

    default T addMethod(String name, Consumer<MethodSpec.Builder> methodFactory) {
        return addMethod(StandaloneMethodFactory.wrap(name, methodFactory));
    }

    T addField(FieldSpec fieldSpec);

    // ######## Field modifications ########
    T addField(TypeName type, String name, FieldFactory fieldFactory);

    default T addField(Type type, String name, FieldFactory fieldFactory) {
        return addField(ClassName.get(type), name, fieldFactory);
    }

    default T addField(Type type, String name, Consumer<FieldSpec.Builder> consumer) {
        return addField(ClassName.get(type), name, FieldFactory.wrap(consumer));
    }

    default T addField(TypeName type, String name, Consumer<FieldSpec.Builder> consumer) {
        return addField(type, name, FieldFactory.wrap(consumer));
    }

    boolean requiresReflectionFor(Element element);
}
