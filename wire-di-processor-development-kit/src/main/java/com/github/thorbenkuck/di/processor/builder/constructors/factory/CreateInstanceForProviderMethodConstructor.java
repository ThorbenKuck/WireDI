package com.github.thorbenkuck.di.processor.builder.constructors.factory;

import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.constructors.MethodConstructor;
import com.github.thorbenkuck.di.runtime.WireRepository;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateInstanceForProviderMethodConstructor implements MethodConstructor {
	@Override
	public String methodName() {
		return CreateInstanceMethodConstructor.METHOD_NAME;
	}

	@Override
	public void construct(WireInformation wireInformation, TypeSpec.Builder typeBuilder) {
		TypeElement primaryWireType = wireInformation.getPrimaryWireType();
		typeBuilder.addMethod(
				MethodSpec.methodBuilder(methodName())
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addParameter(
								ParameterSpec.builder(ClassName.get(WireRepository.class), "wireRepository", Modifier.FINAL)
										.addAnnotation(NotNull.class)
										.build()
						)
						.returns(TypeName.get(primaryWireType.asType()))
						.addCode(buildMethodBody(wireInformation))
						.build()
		);
	}

	private CodeBlock buildMethodBody(WireInformation wireInformation) {
		String builderName = "builder";
		ExecutableElement builderMethod = wireInformation.getBuilderMethod()
				.orElseThrow(() -> new IllegalStateException("Just... How?"));

		CodeBlock.Builder builder = CodeBlock.builder()
				.addStatement("$T $L = wireRepository.get($T.class)",
						wireInformation.getAnnotationRootType(),
						builderName,
						wireInformation.getAnnotationRootType());

		if (builderMethod.getParameters().isEmpty()) {
			return builder.addStatement("return $L.$L", builderName, builderMethod).build();
		}

		List<String> parameterNames = new ArrayList<>();
		AtomicInteger counter = new AtomicInteger(0);
		builderMethod.getParameters().forEach(parameter -> {
			String name = "param" + counter.incrementAndGet();
			parameterNames.add(name);
			builder.addStatement("$T $L = wireRepository.get($T.class)", ClassName.get(parameter.asType()), name, ClassName.get(parameter.asType()));
		});

		String arguments = String.join(", ", parameterNames);
		builder.addStatement("return $L.$L($L)", builderName, builderMethod.getSimpleName(), arguments);

		return builder.build();
	}
}
