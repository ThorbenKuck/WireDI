package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.ReflectionsHelper;
import com.github.thorbenkuck.di.annotations.aspects.Pure;
import com.github.thorbenkuck.di.aspects.AspectExecutionContext;
import com.github.thorbenkuck.di.processor.Aop;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.utils.TypeSpecs;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AspectAwareProxyMethodOverwrite {

    private final WireInformation wireInformation;

    public AspectAwareProxyMethodOverwrite(WireInformation wireInformation) {
        this.wireInformation = wireInformation;
    }

    public static List<ExecutableElement> findEligibleMethods(WireInformation wireInformation) {
        return wireInformation.getWireCandidate().getEnclosedElements()
                .stream()
                .filter(it -> it.getKind() == ElementKind.METHOD)
                .filter(it -> it.getAnnotation(Pure.class) == null)
                .map(it -> (ExecutableElement) it)
                .filter(it -> !it.getModifiers().contains(Modifier.PRIVATE))
                .filter(it -> !it.getModifiers().contains(Modifier.FINAL))
                // This process can later work, but needs additional work
                .filter(it -> !it.getModifiers().contains(Modifier.ABSTRACT))
                .collect(Collectors.toList());
    }

    public void overwriteAllFor(TypeSpec.Builder proxyClassBuilder) {
        List<ExecutableElement> allPublicMethods = findEligibleMethods(wireInformation);
        if (allPublicMethods.isEmpty()) {
            // There is nothing to proxy.
            return;
        }

        AtomicInteger roundCounter = new AtomicInteger(0);
        for (ExecutableElement publicMethod : allPublicMethods) {
            Optional<MethodProxyInformation> aspectInvokingCodeBlock = tryProxyMethod(publicMethod, roundCounter.getAndIncrement());

            aspectInvokingCodeBlock.ifPresent(methodProxyInformation -> {
                proxyClassBuilder.addMethod(methodProxyInformation.methodSpec);
                proxyClassBuilder.addFields(methodProxyInformation.annotationValueFields);
            });
        }
    }

    private Optional<MethodProxyInformation> tryProxyMethod(ExecutableElement current, int methodCounter) {
        List<? extends AnnotationMirror> annotationMirrors = Aop.getAopEnabledAnnotations(current);
        if (annotationMirrors.isEmpty()) {
            return Optional.empty();
        }

        TypeName returnType = ClassName.get(current.getReturnType());
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(current)
                .addModifiers(Modifier.FINAL)
                .addStatement("final $T aspectContext = aspectRepository.startBuilder($L)", AspectExecutionContext.class, invokeSuperLambda(current))
                .returns(returnType);

        List<FieldSpec> fields = new ArrayList<>();
        methodBuilder.addCode("\n")
                .addCode("// Right here we will utilize the ExecutionContextBuilder to make reading easier.\n");


        AtomicInteger roundCounter = new AtomicInteger(0);
        annotationMirrors.forEach(mirror -> {
            roundCounter.incrementAndGet();
            TypeName annotationType = ClassName.get(mirror.getAnnotationType());
            String annotationName = "annotation" + methodCounter + "" + roundCounter.get();
            fields.add(
                    FieldSpec.builder(annotationType, annotationName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .addAnnotation(AnnotationSpec.builder(Nullable.class).build())
                            .initializer("$T.findAnnotationOnMethod($T.class, $T.class, $S, $L, $T.class)",
                                    ClassName.get(ReflectionsHelper.class),
                                    ClassName.get(wireInformation.getPrimaryWireType()),
                                    annotationType,
                                    current.getSimpleName(),
                                    TypeSpecs.parametersAsClassArrayInstance(current),
                                    returnType
                            )
                            .build()
            );

            methodBuilder.addStatement("aspectContext.announceInterestForAspect($T.class, $L)", annotationType, annotationName);
        });

        methodBuilder
                .addCode("\n")
                .beginControlFlow("if(aspectContext.noAspectsPresent())")
                .addCode(simplyInvokeSuper(current))
                .endControlFlow()
                .addCode("\n");

        current.getParameters().forEach(parameter -> {
            methodBuilder.addStatement("aspectContext.declareArgument($S, $L)", parameter.getSimpleName(), parameter.getSimpleName());
        });

        List<? extends AnnotationMirror> nullableAnnotations = current.getAnnotationMirrors()
                .stream()
                .filter(it -> it.getAnnotationType().asElement().getSimpleName().toString().equals("Nullable"))
                .collect(Collectors.toList());
        boolean mayBeNull = current.getReturnType().getKind().equals(TypeKind.VOID) || !nullableAnnotations.isEmpty();

        if (willReturnSomething(current)) {
            methodBuilder.addStatement("return ($T) aspectContext.run($L)", ClassName.get(current.getReturnType()), mayBeNull);
        } else {
            methodBuilder.addStatement("aspectContext.run($L)", mayBeNull);
        }

        return Optional.of(new MethodProxyInformation(methodBuilder.build(), fields));
    }

    private boolean willReturnSomething(ExecutableElement executableElement) {
        return executableElement.getReturnType().getKind() != TypeKind.VOID;
    }

    private CodeBlock invokeSuperLambda(ExecutableElement executableElement) {
        CodeBlock.Builder invokeSuperLambda = CodeBlock.builder();
        invokeSuperLambda.add("context -> {\n").indent();

        List<String> variableNames = new ArrayList<>();
        AtomicInteger parameterCounter = new AtomicInteger(0);

        for (VariableElement parameter : executableElement.getParameters()) {
            String varName = "parameter" + parameterCounter.incrementAndGet();
            // Casting additionally is required right
            // here, because the compiler really does
            // not like it, if we don't. It can never be of
            // another type, since the "requireArgumentAs"
            // Already validates AND casts the type.
            invokeSuperLambda.add("$T $L = ($T) context.requireArgumentAs($S, $T.class);\n", ClassName.get(parameter.asType()), varName, ClassName.get(parameter.asType()), parameter.getSimpleName(), ClassName.get(parameter.asType()));
            variableNames.add(varName);
        }

        if (willReturnSomething(executableElement)) {
            invokeSuperLambda.add("return super.$L($L);\n", executableElement.getSimpleName(), String.join(", ", variableNames));
        } else {
            invokeSuperLambda.add("super.$L($L);\n", executableElement.getSimpleName(), String.join(", ", variableNames))
                    .add("return null; // Nothing to return, so just return null\n");
        }

        invokeSuperLambda.unindent().add("}");
        return invokeSuperLambda.build();
    }

    private CodeBlock simplyInvokeSuper(ExecutableElement executableElement) {
        String variableNames = executableElement.getParameters()
                .stream().map(VariableElement::getSimpleName)
                .collect(Collectors.joining(","));
        CodeBlock.Builder result = CodeBlock.builder();

        if (willReturnSomething(executableElement)) {
            result.addStatement("return super.$L($L)", executableElement.getSimpleName(), variableNames);
        } else {
            result.addStatement("super.$L($L)", executableElement.getSimpleName(), variableNames)
                    .addStatement("return");
        }

        return result.build();
    }

    private static class MethodProxyInformation {
        private final MethodSpec methodSpec;
        private final List<FieldSpec> annotationValueFields;

        private MethodProxyInformation(MethodSpec methodSpec, List<FieldSpec> annotationValueFields) {
            this.methodSpec = methodSpec;
            this.annotationValueFields = annotationValueFields;
        }
    }
}
