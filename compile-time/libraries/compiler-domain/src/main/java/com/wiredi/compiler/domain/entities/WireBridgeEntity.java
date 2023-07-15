package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.injection.FieldInjectionPoint;
import com.wiredi.compiler.domain.injection.MethodInjectionPoint;
import com.wiredi.compiler.domain.injection.NameContext;
import com.wiredi.compiler.domain.injection.VariableContext;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class WireBridgeEntity extends AbstractClassEntity {

	private final NameContext nameContext = new NameContext();
	private final WireRepositories wireRepositories;

	public WireBridgeEntity(TypeMirror rootElement, String className, WireRepositories wireRepositories) {
		super(rootElement, className);
		this.wireRepositories = wireRepositories;
	}

	@Override
	protected TypeSpec.Builder createBuilder(TypeMirror type) {
		return TypeSpec.classBuilder(className)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
	}

	public String bridgePackagePrivateField(FieldInjectionPoint injectionPoint) {
		String methodName = nameContext.nextName(injectionPoint.name());
		QualifierType qualifier = Qualifiers.injectionQualifier(injectionPoint.field());
		CodeBlock fieldValue = wireRepositories.fetchFromWireRepository(injectionPoint.type(), qualifier);

		builder.addMethod(
				MethodSpec.methodBuilder(methodName)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.addParameter(
								ParameterSpec.builder(WireRepository.class, "wireRepository")
										.addAnnotation(NotNull.class)
										.addModifiers(Modifier.FINAL)
										.build()
						).addParameter(
								ParameterSpec.builder(ClassName.get(injectionPoint.getDeclaringClass()), "instance")
										.addAnnotation(NotNull.class)
										.addModifiers(Modifier.FINAL)
										.build()
						)
						.addStatement("instance.$L = $L", injectionPoint.name(), fieldValue)
						.build()
		);
		return methodName;
	}

	public String bridgePackagePrivateMethod(MethodInjectionPoint injectionPoint) {
		String methodName = nameContext.nextName(injectionPoint.name());
		CodeBlock.Builder methodBody = CodeBlock.builder();
		VariableContext variableContext = new VariableContext();
		String fetchVariables = getVariablesFromWireRepository(methodBody, injectionPoint.parameters(), variableContext);
		CodeBlock body = methodBody.addStatement("instance.$L($L)", injectionPoint.name(), fetchVariables).build();

		builder.addMethod(
				MethodSpec.methodBuilder(methodName)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.addParameter(
								ParameterSpec.builder(WireRepository.class, "wireRepository")
										.addAnnotation(NotNull.class)
										.addModifiers(Modifier.FINAL)
										.build()
						).addParameter(
								ParameterSpec.builder(ClassName.get(injectionPoint.getDeclaringClass()), "instance")
										.addAnnotation(NotNull.class)
										.addModifiers(Modifier.FINAL)
										.build()
						)
						.addCode(body)
						.build()
		);
		return methodName;
	}

	private String getVariablesFromWireRepository(
			CodeBlock.Builder rootCodeBlock,
			List<? extends VariableElement> parameters,
			VariableContext variableContext
	) {
		List<String> fetchVariables = new ArrayList<>();

		parameters.forEach(parameter -> {
			String variableName = variableContext.instantiateVariableIfRequired(parameter, wireRepositories, rootCodeBlock);
			fetchVariables.add(variableName);
		});

		return String.join(", ", fetchVariables);
	}

}
