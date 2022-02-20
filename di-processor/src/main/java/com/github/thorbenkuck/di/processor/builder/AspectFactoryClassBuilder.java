package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.domain.WireRepository;
import com.github.thorbenkuck.di.aspects.AspectInstance;
import com.github.thorbenkuck.di.domain.AspectFactory;
import com.github.thorbenkuck.di.processor.ClassWriter;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.time.ZonedDateTime;

public class AspectFactoryClassBuilder {

    private final TypeSpec.Builder typeSpecBuilder;
    private final TypeSpec aspectInstanceClass;
    private final String aspectInstanceClassName;
    private final TypeMirror annotationType;
    private final TypeElement wrappedClass;

    private static final ClassName PROVIDER_CLASS_NAME = ClassName.get(AspectFactory.class);
    private static final AnnotationSpec AUTO_SERVICE_ANNOTATION_SPEC = AnnotationSpec.builder(AutoService.class)
            .addMember("value", "$T.class", AspectFactory.class)
            .build();

    public AspectFactoryClassBuilder(TypeSpec aspectInstanceClass, TypeMirror annotationType, TypeElement wrappedClass) {
        this.aspectInstanceClass = aspectInstanceClass;
        this.aspectInstanceClassName = aspectInstanceClass.name;
        this.annotationType = annotationType;
        this.wrappedClass = wrappedClass;
        this.typeSpecBuilder = TypeSpec.classBuilder(aspectInstanceClassName + "Factory")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(PROVIDER_CLASS_NAME, ClassName.get(annotationType)))
                .addAnnotation(AUTO_SERVICE_ANNOTATION_SPEC);
    }

    public AspectFactoryClassBuilder appendAspectInstanceClass() {
        typeSpecBuilder.addType(aspectInstanceClass);

        return this;
    }

    public AspectFactoryClassBuilder addBuildMethod() {
        typeSpecBuilder.addMethod(
                MethodSpec.methodBuilder("build")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(ParameterizedTypeName.get(ClassName.get(AspectInstance.class), ClassName.get(annotationType)))
                        .addParameter(
                                ParameterSpec.builder(ClassName.get(WireRepository.class), "wireRepository")
                                        .addModifiers(Modifier.FINAL)
                                        .build()
                        )
                        .addCode(CodeBlock.builder()
                                .addStatement("$T dependency = wireRepository.requireInstance($T.class)", ClassName.get(wrappedClass), ClassName.get(wrappedClass))
                                .addStatement("return new $L(dependency)", aspectInstanceClassName)
                                .build())
                        .build()
        );

        return this;
    }

    public AspectFactoryClassBuilder markAsGenerated(String... comments) {
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", getClass().getName())
                .addMember("date", "$S", ZonedDateTime.now().toString());
        if (comments.length > 0) {
            annotationBuilder.addMember("comments", "$S", String.join("\n", comments));
        }
        typeSpecBuilder.addAnnotation(annotationBuilder.build());

        return this;
    }

    public void writeClass() {
        ClassWriter.write(typeSpecBuilder.build(), wrappedClass);
    }

    public AspectFactoryClassBuilder addAroundAnnotationMethod() {
        ParameterizedTypeName annotationClass = ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get(annotationType));
        typeSpecBuilder.addField(FieldSpec.builder(annotationClass, "TYPE")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                        .initializer("$T.class", ClassName.get(annotationType))
                .build());
        typeSpecBuilder.addMethod(MethodSpec.methodBuilder("aroundAnnotation")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(annotationClass)
                .addAnnotation(Override.class)
                        .addCode(CodeBlock.builder()
                                .addStatement("return TYPE")
                                .build())
                .build());

        return this;
    }
}
