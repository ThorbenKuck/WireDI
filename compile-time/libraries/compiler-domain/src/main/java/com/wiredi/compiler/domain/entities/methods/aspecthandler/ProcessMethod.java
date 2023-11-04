package com.wiredi.compiler.domain.entities.methods.aspecthandler;

import com.squareup.javapoet.*;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionContext;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.compiler.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

public class ProcessMethod implements StandaloneMethodFactory {

    private static final Logger logger = Logger.get(ProcessMethod.class);
    private final ExecutableElement methodDelegate;
    private final TypeElement handlerType;
    private final Aspect aspectAnnotation;
    private final TypeMirror executionContextType;
    private final Types types;

    public ProcessMethod(
            ExecutableElement methodDelegate,
            TypeElement handlerType,
            Aspect aspectAnnotation,
            Elements elements,
            Types types) {
        this.methodDelegate = methodDelegate;
        this.handlerType = handlerType;
        this.aspectAnnotation = aspectAnnotation;
        this.executionContextType = types.erasure(elements.getTypeElement(ExecutionContext.class.getName()).asType());
        this.types = types;
    }

    private List<CodeBlock> getParameterList(List<ConstructorParameter> constructorParameterList) {
        final List<CodeBlock> parameters = new ArrayList<>();
        methodDelegate.getParameters().forEach(parameter -> {
            if (types.erasure(parameter.asType()).equals(executionContextType)) {
                parameters.add(CodeBlock.of("context"));
            } else {
                ConstructorParameter constructorParameter = new ConstructorParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
                constructorParameterList.add(constructorParameter);
                parameters.add(CodeBlock.of(constructorParameter.fieldName));
            }
        });
        return parameters;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        final CodeBlock.Builder invokeDelegate = CodeBlock.builder();
        final List<ConstructorParameter> parameterList = new ArrayList<>();

        if (methodDelegate.getReturnType().getKind() != TypeKind.NONE) {
            invokeDelegate.add("return ");
        }

        if (methodDelegate.getParameters().isEmpty()) {
            invokeDelegate.addStatement("delegate.$L()", methodDelegate.getSimpleName());
        } else {
            List<CodeBlock> parameters = getParameterList(parameterList);
            invokeDelegate.addStatement("delegate.$L($L)", methodDelegate.getSimpleName(), joinAsMethodInvocation(parameters));
        }

        entity.addInterface(ClassName.get(AspectHandler.class))
                .addField(TypeName.get(handlerType.asType()), "delegate", field -> field.addModifiers(Modifier.PRIVATE, Modifier.FINAL));

        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addAnnotation(Nullable.class)
                .returns(Object.class)
                .addParameter(
                        ParameterSpec.builder(ClassName.get(ExecutionContext.class), "context")
                                .addModifiers(Modifier.FINAL)
                                .addAnnotation(NotNull.class)
                                .build()
                )
                .addCode(invokeDelegate.build());

        entity.addField(TypeName.get(handlerType.asType()), "delegate", field -> field.addModifiers(Modifier.PRIVATE, Modifier.FINAL));

        entity.setConstructor((constructor, e) -> {
            constructor.addModifiers(Modifier.PROTECTED)
                    .addParameter(TypeName.get(handlerType.asType()), "delegate")
                    .addStatement("this.delegate = delegate");
            parameterList.forEach(parameter -> {
                entity.addField(parameter.typeName, parameter.fieldName, field -> field.addModifiers(Modifier.FINAL));
                constructor.addParameter(parameter.typeName, parameter.fieldName);
                constructor.addStatement("this.$L = $L", parameter.fieldName, parameter.fieldName);
            });
        });
    }

    private CodeBlock joinAsMethodInvocation(List<CodeBlock> codeBlocks) {
        if (codeBlocks.isEmpty()) {
            return CodeBlock.builder().build();
        } else if (codeBlocks.size() == 1) {
            return codeBlocks.get(0);
        } else {
            return CodeBlock.builder()
                    .indent()
                    .add("\n")
                    .add(CodeBlock.join(codeBlocks, ",\n"))
                    .add("\n")
                    .unindent()
                    .build();
        }
    }

    @Override
    public String methodName() {
        return "process";
    }

    record ConstructorParameter(TypeName typeName, String fieldName) {
    }
}
