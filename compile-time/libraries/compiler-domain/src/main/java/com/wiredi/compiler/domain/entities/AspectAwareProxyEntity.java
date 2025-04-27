package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.aspects.AspectHandler;
import com.wiredi.runtime.aspects.ExecutionChain;
import com.wiredi.runtime.aspects.ExecutionChainRegistry;
import com.wiredi.runtime.aspects.RootMethod;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.AnnotationMetaDataSpec;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.injection.NameContext;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.runtime.domain.aop.AspectAwareProxy;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.values.Value;
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

    private static final String EXECUTION_CHAIN_REGISTRY_PARAMETER_NAME = "executionChainRegistry";

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

        if (proxyMethod.parameters().isEmpty() && proxyMethod.proxyAnnotations().isEmpty()) {
            return codeBlock
                    .add("just($S, $L)", methodName, rootMethodInvocation(proxyMethod))
                    .build();
        }
        codeBlock.add("builder($S)\n", methodName).indent();
        proxyMethod.parameters().forEach(parameter -> {
            CodeBlock type = typeIdentifiers.newTypeIdentifier(parameter.asType());
            codeBlock.add(".withParameter($S, $L)\n", parameter.getSimpleName(), type);
        });
        proxyMethod.proxyAnnotations().forEach(annotationMirror -> {
            CodeBlock metaData = AnnotationMetaDataSpec.initializer(annotationMirror);
            codeBlock.add(".withAnnotation($L)\n", metaData);
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
        TypeName returnType = ClassName.get(proxyMethod.returnType());
        MethodSpec.Builder methodBuilder = MethodSpecs.override(proxyMethod.value())
                .addModifiers(Modifier.FINAL)
                .returns(returnType);

        if (proxyMethod.proxyAnnotations().isEmpty()) {
            builder.addMethod(
                    methodBuilder.addCode(
                            CodeBlock.builder().add("return super.$L(", proxyMethod.simpleName())
                                    .add("$L", proxyMethod.parameters().stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", ")))
                                    .addStatement(")")
                                    .build()
                    ).build()
            );
            return;
        }

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
                .add("$L.getExecutionChain(\n", EXECUTION_CHAIN_REGISTRY_PARAMETER_NAME)
                .indent()
                .add("$L", createRootMethod(proxyMethod))
                .unindent().add("\n)\n");

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
                                ""
                        ))
                        .add(".andReturn()")
                        .unindent()
                        .build()
        );
        builder.addMethod(methodBuilder.build());
    }

    public AspectAwareProxyEntity addWiredAnnotationFor(List<TypeMirror> types) {
        List<CodeBlock> wireValues = new ArrayList<>();
        types.forEach(type -> wireValues.add(CodeBlock.of("$T.class", TypeName.get(type))));
        wireValues.add(CodeBlock.of("$T.class", compileFinalClassName()));

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
        CodeBlock.Builder manualInitialization = CodeBlock.builder();
        ConstructorBuilder constructorBuilder = new ConstructorBuilder();
        constructorBuilder.addParameter(TypeName.get(ExecutionChainRegistry.class), EXECUTION_CHAIN_REGISTRY_PARAMETER_NAME);
        constructorBuilder.initializeField(TypeName.get(WireRepository.class), WIRE_REPOSITORY_FIELD_NAME);

        // Inherit parameters and call super as the first thing in the constructor
        if (inheritedConstructor != null) {
            List<? extends VariableElement> typeParameters = inheritedConstructor.getParameters();
            constructorBuilder.addParameters(typeParameters, true);
        }

        fieldEntities.forEach(it -> {
            if (it.takeAsConstructorParameter) {
                constructorBuilder.initializeField(it.type, it.name);
            } else {
                manualInitialization.addStatement(it.initializer().build());
            }
        });

        MethodSpec constructor = constructorBuilder.initializeConstructor(manualInitialization.build()).build();
        builder.addMethod(constructor);

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
