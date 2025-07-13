package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.injection.InjectionPoints;
import com.wiredi.compiler.domain.injection.VariableContext;
import com.wiredi.compiler.domain.values.FactoryMethod;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class CreateInstanceForFactoryMethod extends CreateInstanceMethodFactory {

    private final FactoryMethod factoryMethod;
    private final WireRepositories wireRepositories;
    private final InjectionPoints injectionPoints;

    public CreateInstanceForFactoryMethod(
            FactoryMethod factoryMethod,
            CompilerRepository compilerRepository,
            WireRepositories wireRepositories,
            InjectionPoints injectionPoints
    ) {
        super(compilerRepository, wireRepositories);
        this.factoryMethod = factoryMethod;
        this.wireRepositories = wireRepositories;
        this.injectionPoints = injectionPoints;
    }

    @Override
    public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {

        final VariableContext variableContext = new VariableContext();
        final CodeBlock.Builder methodBody = CodeBlock.builder()
                .add("$T builder = ", factoryMethod.enclosingType().asType())
                .addStatement("$L", wireRepositories.fetchFromWireRepository(factoryMethod.enclosingType()));

        final List<String> params = new ArrayList<>();
        factoryMethod.method()
                .getParameters()
                .forEach(parameter -> {
                    if (parameter.asType().toString().startsWith(TypeIdentifier.class.getName())) {
                        // TODO: This check currently only validates, that the class is an identifiable provider.
                        // We need additional security checks,
                        // to make sure that the identifiable provider actually is assignable
                        params.add("concreteType");
                    } else {
                        String variableName = variableContext.instantiateVariableIfRequired(parameter, wireRepositories, methodBody);
                        params.add(variableName);
                    }
                });

        methodBody.addStatement("$T instance = builder.$L($L)", entity.rootType(), factoryMethod.name(), String.join(", ", params));

        builder.returns(TypeName.get(factoryMethod.returnType()))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(WireContainer.class, "wireContainer", Modifier.FINAL)
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeIdentifier.class), TypeName.get(entity.rootType())), "concreteType", Modifier.FINAL)
                .addCode(methodBody.build())
                .addCode(fieldInjectionStep(injectionPoints.fieldInjections(), entity, variableContext))
                .addCode(methodInjectionStep(injectionPoints.methodInjections(), entity, variableContext))
                .addCode(postConstruct(injectionPoints.postConstructInjectionPoints(), entity, variableContext))
                .addStatement("return instance")
                .build();
    }
}
