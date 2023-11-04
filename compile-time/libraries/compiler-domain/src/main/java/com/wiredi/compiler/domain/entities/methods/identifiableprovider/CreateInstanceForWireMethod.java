package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.injection.ConstructorInjectionPoint;
import com.wiredi.compiler.domain.injection.InjectionPoints;
import com.wiredi.compiler.domain.injection.VariableContext;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class CreateInstanceForWireMethod extends CreateInstanceMethodFactory {

    private static final Logger LOGGER = Logger.get(CreateInstanceForWireMethod.class);
    private final InjectionPoints injectionPoints;
    private final WireRepositories wireRepositories;
    private final CompilerRepository compilerRepository;

    public CreateInstanceForWireMethod(
            InjectionPoints injectionPoints,
            WireRepositories wireRepositories,
            CompilerRepository compilerRepository
    ) {
        super(compilerRepository, wireRepositories);
        this.injectionPoints = injectionPoints;
        this.wireRepositories = wireRepositories;
        this.compilerRepository = compilerRepository;
    }

    @Override
    public void append(
            MethodSpec.Builder builder,
            ClassEntity<?> entity
    ) {
        VariableContext variableContext = new VariableContext();

        builder.returns(TypeName.get(entity.rootType()))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(WireRepository.class, "wireRepository", Modifier.FINAL)
                .addCode(constructorInvocationStep(injectionPoints.constructorInjectionPoint(), entity, variableContext))
                .addCode(fieldInjectionStep(injectionPoints.fieldInjections(), entity, variableContext))
                .addCode(methodInjectionStep(injectionPoints.methodInjections(), entity, variableContext))
                .addCode(postConstruct(injectionPoints.postConstructInjectionPoints(), entity, variableContext))
                .addStatement("return instance")
                .build();
    }

    private CodeBlock constructorInvocationStep(
            @Nullable ConstructorInjectionPoint constructorInjectionPoint,
            ClassEntity<?> entity,
            @NotNull VariableContext context
    ) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        List<String> parameters = new ArrayList<>();

        if (constructorInjectionPoint != null) {
            int parameterCount = constructorInjectionPoint.constructor().getParameters().size();
            if (parameterCount > 0) {
                codeBlockBuilder.add("// We will relativeStart by Fetching all $L constructor parameters\n", parameterCount);
                constructorInjectionPoint.constructor().getParameters().forEach(parameter -> {
                    String varName = context.instantiateVariableIfRequired(parameter, wireRepositories, codeBlockBuilder);
                    parameters.add(varName);
                });
            }
        }

        String parameterNames = String.join(",", parameters);
        return codeBlockBuilder.addStatement("$T instance = new $T($L)", entity.rootType(), entity.rootType(), parameterNames).build();
    }
}
