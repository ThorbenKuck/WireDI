package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.processor.ClassWriter;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ClassBuilder {

    private final PackageElement packageElement;
    private TypeSpec.Builder rootBuilder;

    protected ClassBuilder(PackageElement packageElement) {
        this.packageElement = packageElement;
    }

    protected ClassBuilder(WireInformation wireInformation) {
        this(wireInformation.getTargetPackage());
    }

    protected void setup() {
        this.rootBuilder = initialize();
    }

    protected TypeSpec.Builder classBuilder() {
        if(this.rootBuilder == null) {
            throw new IllegalStateException("You forgot to call \"setup\" in your constructor");
        }
        return this.rootBuilder;
    }

    protected void addMethod(MethodSpec.Builder methodBuilder) {
        classBuilder().addMethod(methodBuilder.build());
    }

    protected void addField(FieldSpec.Builder fieldBuilder) {
        classBuilder().addField(fieldBuilder.build());
    }

    protected void addAnnotation(AnnotationSpec.Builder annotationBuilder) {
        classBuilder().addAnnotation(annotationBuilder.build());
    }

    protected FieldSpec.Builder classConstant(TypeName typeName, String name) {
        return FieldSpec.builder(typeName, name)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
    }

    protected ParameterizedTypeName genericClass(TypeElement typeElement) {
        return ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get(typeElement));
    }

    protected abstract TypeSpec.Builder initialize();

    protected Class<?> autoServiceType() {
        return null;
    }

    protected static List<ParameterSpec> parametersOf(ExecutableElement method) {
        return method.getParameters()
                .stream()
                .map(ParameterSpec::get)
                .collect(Collectors.toList());
    }


    private void markAsGenerated(String... comments) {
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", getClass().getName())
                .addMember("date", "$S", ZonedDateTime.now().toString());

        if (comments.length > 0) {
            annotationBuilder.addMember("comments", "$S", String.join("\n", comments));
        }

        classBuilder().addAnnotation(annotationBuilder.build());
    }


    public TypeSpec build(String... comments) {
        markAsGenerated(comments);
        Class<?> autoServiceType = autoServiceType();

        if (autoServiceType != null) {
            addAnnotation(AnnotationSpec.builder(AutoService.class)
                    .addMember("value", "$T.class", autoServiceType)
            );
        }

        return rootBuilder.build();
    }

    public TypeSpec buildAndWrite(String... comments) {
        TypeSpec typeSpec = build();
        ClassWriter.write(typeSpec, packageElement);
        return typeSpec;
    }

    protected MethodSpec.Builder overwriteMethod(String name) {
        return MethodSpec.methodBuilder(name)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    protected MethodSpec.Builder overwriteMethod(ExecutableElement executableElement) {
        return MethodSpec.overriding(executableElement);
    }
}
