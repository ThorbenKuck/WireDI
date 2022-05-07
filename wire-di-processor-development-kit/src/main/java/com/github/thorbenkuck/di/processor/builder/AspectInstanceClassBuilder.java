package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.aspects.AspectInstance;
import com.github.thorbenkuck.di.aspects.ExecutionContext;
import com.github.thorbenkuck.di.processor.ProcessorContext;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Locale;

public class AspectInstanceClassBuilder {

    private final TypeMirror annotationType;
    private final TypeElement containingClass;
    private final ExecutableElement methodToExecute;
    private final TypeSpec.Builder typeSpecBuilder;
    private final String className;

    private static final String delegateName = "delegate";

    public AspectInstanceClassBuilder(TypeMirror annotationType, TypeElement containingClass, ExecutableElement methodToExecute) {
        this.annotationType = annotationType;
        this.containingClass = containingClass;
        this.methodToExecute = methodToExecute;
        TypeElement typeElement = ProcessorContext.mapWithElements(elements -> elements.getTypeElement(annotationType.toString()));
        this.className = typeElement.getSimpleName() +
                "On" + capitalize(methodToExecute.getSimpleName()) +
                "In" + containingClass.getSimpleName() +
                "Aspect";

        typeSpecBuilder = TypeSpec.classBuilder(this.className)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AspectInstance.class), ClassName.get(annotationType)));
    }

    private static String capitalize(Name input) {
        String inputString = input.toString();
        return inputString.substring(0, 1).toUpperCase(Locale.ROOT) + inputString.substring(1);
    }

    private ParameterizedTypeName aspectContextType() {
        return ParameterizedTypeName.get(ClassName.get(ExecutionContext.class), ClassName.get(annotationType));
    }

    public AspectInstanceClassBuilder addDelegateField() {
        typeSpecBuilder
                .addField(
                        FieldSpec.builder(ClassName.get(containingClass), delegateName)
                                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                                .build()
                );

        return this;
    }

    public AspectInstanceClassBuilder addConstructor() {
        typeSpecBuilder
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addParameter(ClassName.get(containingClass), delegateName, Modifier.FINAL)
                                .addModifiers(Modifier.PRIVATE)
                                .addCode(CodeBlock.builder()
                                        .addStatement("this.$L = $L", delegateName, delegateName)
                                        .build())
                                .build()
                );

        return this;
    }

    public AspectInstanceClassBuilder addProcessMethod() {
        CodeBlock.Builder methodBody = CodeBlock.builder();
        TypeKind returnKind = methodToExecute.getReturnType().getKind();
        if(returnKind == TypeKind.VOID) {
            methodBody.addStatement("$L.$L(context)", delegateName, methodToExecute.getSimpleName())
                    .addStatement("return context.getLastReturnValue()");
        } else {
            methodBody.addStatement("return (Object) $L.$L(context)", delegateName, methodToExecute.getSimpleName());
        }

        typeSpecBuilder.addMethod(
                        MethodSpec.methodBuilder("process")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                .addParameter(
                                        ParameterSpec.builder(aspectContextType(), "context")
                                                .addAnnotation(NotNull.class)
                                                .addModifiers(Modifier.FINAL)
                                                .build())
                                .returns(ClassName.OBJECT)
                                .addCode(methodBody.build())
                                .build()
                );

        return this;
    }

    public TypeSpec build() {
        return typeSpecBuilder.build();
    }
}
