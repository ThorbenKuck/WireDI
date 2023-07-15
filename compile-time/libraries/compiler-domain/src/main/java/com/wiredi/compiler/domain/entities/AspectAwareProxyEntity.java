package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Wire;
import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionChain;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.injection.NameContext;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.domain.aop.AspectAwareProxy;
import com.wiredi.lang.ReflectionsHelper;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AspectAwareProxyEntity extends AbstractClassEntity<AspectAwareProxyEntity> {

	private static final String WIRE_REPOSITORY_FIELD_NAME = "wireRepository";

	private final TypeSpecs typeSpecs = new TypeSpecs();

	private final List<FieldEntity> fieldEntities = new ArrayList<>();

	public AspectAwareProxyEntity(TypeElement element) {
		super(element.asType(), element.getSimpleName() + "$$AspectAwareProxy");
	}

	private FieldEntity attachNewFieldEntity(String fieldName, TypeName fieldType) {
		return attachNewFieldEntity(fieldName, fieldType, true);
	}

	private FieldEntity attachNewFieldEntity(String fieldName, TypeName fieldType, boolean takeAsConstructorParameter) {
		FieldEntity fieldEntity = new FieldEntity(fieldName, fieldType, takeAsConstructorParameter);
		fieldEntities.add(fieldEntity);
		return fieldEntity;
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

		AtomicInteger roundCounter = new AtomicInteger(0);
		FieldEntity executionChain = attachNewFieldEntity(nameContext.nextName("executionChain"), TypeName.get(ExecutionChain.class), false);
		executionChain.initializer()
				.add("this.$L = ", executionChain.name)
				.add("$T.newInstance((c) -> super.$L($L))\n", ExecutionChain.class, proxyMethod.simpleName(), proxyMethod.parameters()
						.stream()
						.map(it -> "c.requireParameter(\"" + it.getSimpleName() + "\")")
						.collect(Collectors.joining(", "))
				).indent();

		proxyMethod.proxyAnnotations().forEach(mirror -> {
			TypeName annotationType = ClassName.get(mirror.getAnnotationType());
			String annotationFieldName = nameContext.nextName("annotation");
			ParameterizedTypeName aspectHandlerTypeName = ParameterizedTypeName.get(ClassName.get(List.class),
					ParameterizedTypeName.get(ClassName.get(AspectHandler.class),
							TypeName.get(mirror.getAnnotationType())
					)
			);
			FieldEntity aspectHandlers = attachNewFieldEntity(nameContext.nextName("aspectHandler"), aspectHandlerTypeName);
			aspectHandlers.initializer().add("this.$L = $L", aspectHandlers.name, aspectHandlers.name);
			builder.addField(
					FieldSpec.builder(aspectHandlers.type, aspectHandlers.name)
							.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
							.build()
			);

			builder.addField(
					FieldSpec.builder(TypeName.get(mirror.getAnnotationType()), annotationFieldName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
							.initializer(CodeBlock.of("$T.findAnnotationOnMethod($T.class, $T.class, $S, $L, $T.class)",
									ClassName.get(ReflectionsHelper.class),
									ClassName.get(rootType()),
									annotationType,
									proxyMethod.simpleName(),
									typeSpecs.parametersAsClassArrayInstance(proxyMethod.value()),
									returnType))
							.build()
			);
			executionChain.initializer()
					.add(".withProcessors($L, $L)\n", annotationFieldName, aspectHandlers.name);

			roundCounter.incrementAndGet();
		});
		executionChain.initializer()
				.unindent()
				.add(".build()");

		builder.addField(
				FieldSpec.builder(executionChain.type, executionChain.name)
						.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
						.build()
		);
		CodeBlock.Builder invokeExecutionChain = CodeBlock.builder();
		if (proxyMethod.willReturnSomething()) {
			invokeExecutionChain.add("return ");
		}
		methodBuilder.addStatement(
				invokeExecutionChain
						.add("$L.execute()\n", executionChain.name)
						.indent()
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
		MethodSpec.Builder constructorMethod = MethodSpec.constructorBuilder();

		// Inherit parameters and call super as the first thing in the constructor
		if (inheritedConstructor != null) {
			List<? extends VariableElement> typeParameters = inheritedConstructor.getEnclosedElements()
					.stream()
					.filter(it -> it.getKind() == ElementKind.PARAMETER)
					.map(it -> (VariableElement) it)
					.toList();

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
						.addParameter(
								ParameterSpec.builder(WireRepository.class, WIRE_REPOSITORY_FIELD_NAME, Modifier.FINAL)
										.build()
						)
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
		return TypeSpec.classBuilder(className)
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
