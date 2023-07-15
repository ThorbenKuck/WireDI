package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.injection.*;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class CreateInstanceForWireMethod extends CreateInstanceMethodFactory {

	private final InjectionPoints injectionPoints;
	private final WireRepositories wireRepositories;
	private final CompilerRepository compilerRepository;
	private static final Logger LOGGER = Logger.get(CreateInstanceForWireMethod.class);

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
			TypeSpec.Builder builder,
			AbstractClassEntity<?> entity
	) {
		VariableContext variableContext = new VariableContext();

		builder.addMethod(
				MethodSpec.methodBuilder("createInstance")
						.returns(TypeName.get(entity.rootType()))
						.addModifiers(Modifier.PRIVATE)
						.addParameter(WireRepository.class, "wireRepository", Modifier.FINAL)
						.addCode(constructorInvocationStep(injectionPoints.constructorInjectionPoint(), entity, variableContext))
						.addCode(fieldInjectionStep(injectionPoints.fieldInjections(), entity, variableContext))
						.addCode(methodInjectionStep(injectionPoints.methodInjections(), entity, variableContext))
						.addCode(postConstruct(injectionPoints.postConstructInjectionPoints(), entity, variableContext))
						.addStatement("return instance")
						.build()
		);
	}

	private CodeBlock constructorInvocationStep(
			@Nullable ConstructorInjectionPoint constructorInjectionPoint,
			AbstractClassEntity<?> entity,
			@NotNull VariableContext context
	) {
		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
		List<String> parameters = new ArrayList<>();

		if (constructorInjectionPoint != null) {
			int parameterCount = constructorInjectionPoint.constructor().getParameters().size();
			if (parameterCount > 0) {
				codeBlockBuilder.add("// We will start by Fetching all $L constructor parameters\n", parameterCount);
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
