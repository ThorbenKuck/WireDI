package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Wire;
import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionChain;
import com.wiredi.aspects.links.RootMethod;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.AnnotationMetaDataSpec;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.injection.NameContext;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.domain.AnnotationMetaData;
import com.wiredi.domain.aop.AspectAwareProxy;
import com.wiredi.lang.values.Value;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AspectAwareProxyEntity extends AbstractClassEntity<AspectAwareProxyEntity> {

    private static final String WIRE_REPOSITORY_FIELD_NAME = "wireRepository";

    private static final String ASPECT_HANDLER_PARAMETER_NAME = "aspectHandlers";

    private static final Logger logger = Logger.get(AspectAwareProxyEntity.class);

    private final List<FieldEntity> fieldEntities = new ArrayList<>();

    private final TypeIdentifiers typeIdentifiers;

    private final boolean asyncExecutionChainConstruction;

    public AspectAwareProxyEntity(TypeElement element, TypeIdentifiers typeIdentifiers, boolean asyncExecutionChainConstruction) {
        super(element, element.asType(), element.getSimpleName() + "$$AspectAwareProxy");
        this.typeIdentifiers = typeIdentifiers;
        this.asyncExecutionChainConstruction = asyncExecutionChainConstruction;
    }

    private FieldEntity attachNewFieldEntity(String fieldName, TypeName fieldType) {
        return attachNewFieldEntity(fieldName, fieldType, true);
    }

    private FieldEntity attachNewFieldEntity(String fieldName, TypeName fieldType, boolean takeAsConstructorParameter) {
        FieldEntity fieldEntity = new FieldEntity(fieldName, fieldType, takeAsConstructorParameter);
        fieldEntities.add(fieldEntity);
        return fieldEntity;
    }

    private CodeBlock rootMethodInvocation(ProxyMethod proxyMethod) {
        String parameters = proxyMethod.parameters()
                .stream()
                .map(it -> "c.requireParameter(\"" + it.getSimpleName() + "\")")
                .collect(Collectors.joining(", "));
        if (proxyMethod.willReturnSomething()) {
            return CodeBlock.builder()
                    .add("(c) -> super.$L($L)", proxyMethod.simpleName(), parameters)
                    .build();
        } else {
            return CodeBlock.builder()
                    .add("$T.wrap((c) -> super.$L($L))", AspectHandler.class, proxyMethod.simpleName(), parameters)
                    .build();
        }
    }

    private CodeBlock createRootMethod(ProxyMethod proxyMethod) {
        String methodName = proxyMethod.simpleName();
        CodeBlock.Builder codeBlock = CodeBlock.builder().add("$T.", RootMethod.class);

        if (proxyMethod.parameters().isEmpty()) {
            return codeBlock
                    .add("just($S, $L)", methodName, rootMethodInvocation(proxyMethod))
                    .build();
        }
        codeBlock.add("newInstance($S)\n", methodName).indent();
        proxyMethod.parameters().forEach(parameter -> {
            CodeBlock type = typeIdentifiers.newTypeIdentifier(parameter.asType());
            codeBlock.add(".withParameter($S, $L)\n", parameter.getSimpleName(), type);
        });
        return codeBlock
                .add(".build($L)", rootMethodInvocation(proxyMethod))
                .unindent()
                .build();
    }

    public void proxyMethod(
            ProxyMethod proxyMethod,
            NameContext nameContext
    ) {
        if (proxyMethod.proxyAnnotations().isEmpty()) {
            return;
        }
        TypeName returnType = ClassName.get(proxyMethod.returnType());
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(proxyMethod.value())
                .addModifiers(Modifier.FINAL)
                .returns(returnType);

        TypeName fieldType;
        if (asyncExecutionChainConstruction) {
            fieldType = ParameterizedTypeName.get(Value.class, ExecutionChain.class);
        } else {
            fieldType = TypeName.get(ExecutionChain.class);
        }

        FieldEntity executionChain = attachNewFieldEntity(nameContext.nextName("executionChain"), fieldType, false);
        executionChain.initializer()
                .add("this.$L = ", executionChain.name);
        if (asyncExecutionChainConstruction) {
            executionChain.initializer().add("$T.async(() ->\n", Value.class)
                    .indent();
        }
        executionChain.initializer()
                .add("$T.newInstance($L)\n", ExecutionChain.class, createRootMethod(proxyMethod))
                .indent();

        proxyMethod.proxyAnnotations().forEach(mirror -> {
            String annotationFieldName = nameContext.nextName("annotation");
            builder.addField(
                    FieldSpec.builder(AnnotationMetaData.class, annotationFieldName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer(AnnotationMetaDataSpec.initializer(mirror))
                            .build()
            );
            executionChain.initializer()
                    .add(".withProcessors($L, $L)\n", annotationFieldName, ASPECT_HANDLER_PARAMETER_NAME);
        });
        executionChain.initializer()
                .unindent()
                .add(".build()");
        if (asyncExecutionChainConstruction) {
            executionChain.initializer().unindent().add("\n)");
        }

        builder.addField(
                FieldSpec.builder(executionChain.type, executionChain.name)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build()
        );
        CodeBlock.Builder invokeExecutionChain = CodeBlock.builder();
        if (proxyMethod.willReturnSomething()) {
            invokeExecutionChain.add("return ");
        }
        if (asyncExecutionChainConstruction) {
            invokeExecutionChain.add("$L.get()\n", executionChain.name)
                    .indent()
                    .add(".execute()\n");
        } else {
            invokeExecutionChain.add("$L.execute()\n", executionChain.name)
                    .indent();
        }

        methodBuilder.addStatement(
                invokeExecutionChain
                        .add(CodeBlock.join(
                                proxyMethod.parameters()
                                        .stream()
                                        .map(variableElement -> CodeBlock.of(".withParameter($S, $L)\n", variableElement.getSimpleName(), variableElement.getSimpleName()))
                                        .toList(),
                                ", "
                        ))
                        .add(".andReturn()")
                        .unindent()
                        .build()
        );
        builder.addMethod(methodBuilder.build());
    }

    public AspectAwareProxyEntity addWiredAnnotationFor(List<TypeMirror> types) {
        List<CodeBlock> wireValues = new ArrayList<>();
        types.forEach(type -> wireValues.add(CodeBlock.of("$T.class", ClassName.get(type))));

        builder.addAnnotation(
                AnnotationSpec.builder(Wire.class)
                        .addMember("to", "{$L}", CodeBlock.join(wireValues, ", "))
                        .addMember("singleton", "true")
                        .addMember("proxy", "false")
                        .build()
        );

        return this;
    }

    public AspectAwareProxyEntity addConstructorInvocation(@Nullable ExecutableElement inheritedConstructor) {
        CodeBlock.Builder constructorInitializer = CodeBlock.builder();
        MethodSpec.Builder constructorMethod = MethodSpec.constructorBuilder()
                .addParameter(
                        ParameterSpec.builder(ParameterizedTypeName.get(List.class, AspectHandler.class), ASPECT_HANDLER_PARAMETER_NAME)
                                .addModifiers(Modifier.FINAL)
                                .addAnnotation(NotNull.class)
                                .build()
                )
                .addParameter(
                        ParameterSpec.builder(WireRepository.class, WIRE_REPOSITORY_FIELD_NAME)
                                .addModifiers(Modifier.FINAL)
                                .addAnnotation(NotNull.class)
                                .build()
                );

        // Inherit parameters and call super as the first thing in the constructor
        if (inheritedConstructor != null) {
            List<? extends VariableElement> typeParameters = inheritedConstructor.getParameters();

            if (!typeParameters.isEmpty()) {
                List<String> superInvokes = new ArrayList<>();
                typeParameters.forEach(parameter -> {
                    ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(TypeName.get(parameter.asType()), parameter.getSimpleName().toString())
                            .addModifiers(Modifier.FINAL);
                    parameter.getAnnotationMirrors().forEach(mirror -> parameterBuilder.addAnnotation(AnnotationSpec.get(mirror)));
                    constructorMethod.addParameter(parameterBuilder.build());
                    superInvokes.add(parameter.getSimpleName().toString());
                });

                constructorInitializer.addStatement("super($L)", String.join(",", superInvokes));
            }
        }

        fieldEntities.forEach(it -> {
            if (it.takeAsConstructorParameter) {
                constructorMethod.addParameter(
                        ParameterSpec.builder(it.type, it.name)
                                .addModifiers(Modifier.FINAL)
                                .addAnnotation(NotNull.class)
                                .build()
                ).build();
            }
            constructorInitializer.addStatement(it.initializer().build());
        });

        builder.addMethod(
                constructorMethod
                        .addCode(
                                constructorInitializer
                                        .addStatement("this.$L = $L", WIRE_REPOSITORY_FIELD_NAME, WIRE_REPOSITORY_FIELD_NAME)
                                        .build()
                        ).build()
        );

        return this;
    }

    @Override
    protected TypeSpec.Builder createBuilder(TypeMirror type) {
        return TypeSpec.classBuilder(className())
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(AspectAwareProxy.class)
                .superclass(type)
                .addAnnotation(
                        AnnotationSpec.builder(PrimaryWireType.class)
                                .addMember("value", "$T.class", type)
                                .build()
                )
                .addField(
                        FieldSpec.builder(WireRepository.class, WIRE_REPOSITORY_FIELD_NAME, Modifier.PRIVATE, Modifier.FINAL)
                                .build()
                );
    }

    class FieldEntity {
        private final CodeBlock.Builder constructorInitializer = CodeBlock.builder();
        private final String name;
        private final TypeName type;
        private final boolean takeAsConstructorParameter;

        FieldEntity(String name, TypeName type, boolean takeAsConstructorParameter) {
            this.name = name;
            this.type = type;
            this.takeAsConstructorParameter = takeAsConstructorParameter;
        }

        public CodeBlock.Builder initializer() {
            return constructorInitializer;
        }
    }
}
