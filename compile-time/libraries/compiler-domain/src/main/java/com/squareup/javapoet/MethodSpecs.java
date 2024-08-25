package com.squareup.javapoet;

import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.*;

public class MethodSpecs {

    @NotNull
    public static MethodSpec.Builder override(
            @NotNull ExecutableElement method
    ) {
        return override(method, true, true);
    }

    @NotNull
    public static MethodSpec.Builder override(
            @NotNull ExecutableElement method,
            boolean keepMethodAnnotations,
            boolean keepParameterAnnotations
    ) {
        Element enclosingClass = method.getEnclosingElement();
        if (enclosingClass.getModifiers().contains(Modifier.FINAL)) {
            throw new IllegalArgumentException("Cannot override method on final class " + enclosingClass);
        }

        Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE)
                || modifiers.contains(Modifier.FINAL)
                || modifiers.contains(Modifier.STATIC)) {
            throw new IllegalArgumentException("cannot override method with modifiers: " + modifiers);
        }

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString());
        addAnnotations(methodBuilder, method, keepMethodAnnotations);
        addGenerics(methodBuilder, method);

        modifiers = new LinkedHashSet<>(modifiers);
        modifiers.remove(Modifier.ABSTRACT);
        modifiers.remove(Modifier.DEFAULT);
        methodBuilder.addModifiers(modifiers);

        methodBuilder.returns(TypeName.get(method.getReturnType()));
        addParameters(methodBuilder, method, keepParameterAnnotations);
        methodBuilder.varargs(method.isVarArgs());

        for (TypeMirror thrownType : method.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrownType));
        }

        return methodBuilder;
    }

    private static void addAnnotations(
            MethodSpec.Builder methodBuilder,
            ExecutableElement method,
            boolean inheritAnnotations
    ) {
        Set<AnnotationSpec> annotations = new HashSet<>();
        annotations.add(AnnotationSpec.builder(Override.class).build());

        if (inheritAnnotations) {
            method.getAnnotationMirrors().stream().forEach(it -> {
                AnnotationSpec annotationSpec = AnnotationSpec.get(it);
                if (annotations.stream().noneMatch(present -> present.type == annotationSpec.type)) {
                    annotations.add(annotationSpec);
                }
            });
        }

        methodBuilder.addAnnotations(annotations);
    }

    private static void addParameters(
            MethodSpec.Builder methodBuilder,
            ExecutableElement method,
            boolean inheritAnnotations
    ) {
        List<ParameterSpec> result = new ArrayList<>();
        for (VariableElement parameter : method.getParameters()) {
            TypeName type = TypeName.get(parameter.asType());
            String name = parameter.getSimpleName().toString();
            ParameterSpec.Builder parameterSpec = ParameterSpec.builder(type, name)
                    .addModifiers(parameter.getModifiers());

            if (inheritAnnotations) {
                Set<AnnotationSpec> annotations = new HashSet<>();

                parameter.getAnnotationMirrors().forEach(annotationMirror -> {
                    AnnotationSpec annotationSpec = AnnotationSpec.get(annotationMirror);
                    if (annotations.stream().noneMatch(present -> present.type == annotationSpec.type)) {
                        annotations.add(annotationSpec);
                    }
                });

                parameterSpec.addAnnotations(annotations);
            }

            result.add(parameterSpec.build());
        }
        methodBuilder.addParameters(result);
    }

    private static void addGenerics(
            MethodSpec.Builder methodBuilder,
            ExecutableElement method
    ) {
        for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
            TypeVariable var = (TypeVariable) typeParameterElement.asType();
            methodBuilder.addTypeVariable(TypeVariableName.get(var));
        }
    }
}
