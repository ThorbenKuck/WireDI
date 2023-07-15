package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.injection.InjectionPoints;
import com.wiredi.compiler.domain.injection.VariableContext;
import com.wiredi.compiler.domain.values.FactoryMethod;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.WireRepository;

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
	public void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity) {
		final VariableContext variableContext = new VariableContext();
		final CodeBlock.Builder methodBody = CodeBlock.builder()
				.add("$T builder = ", factoryMethod.enclosingType().asType())
				.addStatement("$L", wireRepositories.fetchFromWireRepository(factoryMethod.enclosingType()));

		final List<String> params = new ArrayList<>();
		factoryMethod.method()
				.getParameters()
				.forEach(parameter -> {
					String variableName = variableContext.instantiateVariableIfRequired(parameter, wireRepositories, methodBody);
					params.add(variableName);
				});

		methodBody.addStatement("$T instance = builder.$L($L)", entity.rootType(), factoryMethod.name(), String.join(", ", params));

		builder.addMethod(
				MethodSpec.methodBuilder("createInstance")
						.returns(TypeName.get(factoryMethod.returnType()))
						.addModifiers(Modifier.PRIVATE)
						.addParameter(WireRepository.class, "wireRepository", Modifier.FINAL)
						.addCode(methodBody.build())
						.addCode(fieldInjectionStep(injectionPoints.fieldInjections(), entity, variableContext))
						.addCode(methodInjectionStep(injectionPoints.methodInjections(), entity, variableContext))
						.addCode(postConstruct(injectionPoints.postConstructInjectionPoints(), entity, variableContext))
						.addStatement("return instance")
						.build()
		);
	}
}
