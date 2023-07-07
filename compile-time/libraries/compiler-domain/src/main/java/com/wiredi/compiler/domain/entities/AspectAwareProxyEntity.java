package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Wire;
import com.wiredi.aspects.AspectExecutionContext;
import com.wiredi.aspects.AspectRepository;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.injection.NameContext;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.domain.aop.AspectAwareProxy;
import com.wiredi.lang.ReflectionsHelper;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AspectAwareProxyEntity extends AbstractClassEntity {

	private static final String ASPECT_REPOSITORY_FIELD_NAME = "aspectRepository";

	private final TypeSpecs typeSpecs = new TypeSpecs();

	public AspectAwareProxyEntity(TypeElement element) {
		super(element.asType(), element.getSimpleName() + "$$AspectAwareProxy");
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
				.addStatement("final $T aspectContext = aspectRepository.startBuilder($L)", AspectExecutionContext.class, invokeSuperLambda(proxyMethod))
				.returns(returnType);

		List<FieldSpec> fields = new ArrayList<>();
		methodBuilder.addCode("\n")
				.addCode("// Right here we will utilize the ExecutionContextBuilder to make reading easier.\n");


		AtomicInteger roundCounter = new AtomicInteger(0);
		proxyMethod.proxyAnnotations().forEach(mirror -> {
			roundCounter.incrementAndGet();
			TypeName annotationType = ClassName.get(mirror.getAnnotationType());
			String annotationName = nameContext.nextName("annotation") + roundCounter.get();
			fields.add(
					FieldSpec.builder(annotationType, annotationName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
							.addAnnotation(AnnotationSpec.builder(Nullable.class).build())
							.initializer("$T.findAnnotationOnMethod($T.class, $T.class, $S, $L, $T.class)",
									ClassName.get(ReflectionsHelper.class),
									ClassName.get(rootType()),
									annotationType,
									proxyMethod.simpleName(),
									typeSpecs.parametersAsClassArrayInstance(proxyMethod.value()),
									returnType
							)
							.build()
			);

			methodBuilder.addStatement("aspectContext.announceInterestForAspect($T.class, $L)", annotationType, annotationName);
		});

		methodBuilder
				.addCode("\n")
				.beginControlFlow("if(aspectContext.noAspectsPresent())")
				.addCode(simplyInvokeSuper(proxyMethod))
				.endControlFlow()
				.addCode("\n");

		proxyMethod.value().getParameters().forEach(parameter -> {
			methodBuilder.addStatement("aspectContext.declareArgument($S, $L)", parameter.getSimpleName(), parameter.getSimpleName());
		});

		boolean mayBeNull = proxyMethod.returnType().getKind().equals(TypeKind.VOID) || Annotations.hasByName(proxyMethod.value(), Nullable.class);

		if (proxyMethod.willReturnSomething()) {
			methodBuilder.addStatement("return ($T) aspectContext.run($L)", ClassName.get(proxyMethod.returnType()), mayBeNull);
		} else {
			methodBuilder.addStatement("aspectContext.run($L)", mayBeNull);
		}

		builder.addMethod(methodBuilder.build());
	}

	public AspectAwareProxyEntity addWiredAnnotationFor(List<TypeMirror> types) {
		CodeBlock.Builder value = CodeBlock.builder()
				.add("{ ");
		types.forEach(type -> {
			if (!value.isEmpty()) {
				value.add(", ");
			}
			value.add("$T.class", ClassName.get(type));

		});

		builder.addAnnotation(
				AnnotationSpec.builder(Wire.class)
						.addMember("value", value.add(" }").build())
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

		builder.addMethod(
				constructorMethod
						.addParameter(
								ParameterSpec.builder(AspectRepository.class, ASPECT_REPOSITORY_FIELD_NAME, Modifier.FINAL)
										.build()
						)
						.addCode(
								constructorInitializer
										.addStatement("this.$L = $L", ASPECT_REPOSITORY_FIELD_NAME, ASPECT_REPOSITORY_FIELD_NAME)
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
						FieldSpec.builder(AspectRepository.class, ASPECT_REPOSITORY_FIELD_NAME, Modifier.PRIVATE, Modifier.FINAL)
								.build()
				);
	}

	private CodeBlock invokeSuperLambda(ProxyMethod method) {
		CodeBlock.Builder invokeSuperLambda = CodeBlock.builder();
		invokeSuperLambda.add("context -> {\n").indent();

		List<String> variableNames = new ArrayList<>();
		AtomicInteger parameterCounter = new AtomicInteger(0);

		for (VariableElement parameter : method.parameters()) {
			String varName = "parameter" + parameterCounter.incrementAndGet();
			// Casting additionally is required right
			// here, because the compiler really does
			// not like it, if we don't. It can never be of
			// another type, since the "requireArgumentAs"
			// Already validates AND casts the type.
			invokeSuperLambda.add("$T $L = ($T) context.requireArgumentAs($S, $T.class);\n", ClassName.get(parameter.asType()), varName, ClassName.get(parameter.asType()), parameter.getSimpleName(), ClassName.get(parameter.asType()));
			variableNames.add(varName);
		}

		if (method.willReturnSomething()) {
			invokeSuperLambda.add("return super.$L($L);\n", method.simpleName(), String.join(", ", variableNames));
		} else {
			invokeSuperLambda.add("super.$L($L);\n", method.simpleName(), String.join(", ", variableNames))
					.add("return null; // Nothing to return, so just return null\n");
		}

		invokeSuperLambda.unindent().add("}");
		return invokeSuperLambda.build();
	}

	private CodeBlock simplyInvokeSuper(ProxyMethod proxyMethod) {
		String variableNames = proxyMethod.parameters()
				.stream().map(VariableElement::getSimpleName)
				.collect(Collectors.joining(","));
		CodeBlock.Builder result = CodeBlock.builder();

		if (proxyMethod.willReturnSomething()) {
			result.addStatement("return super.$L($L)", proxyMethod.simpleName(), variableNames);
		} else {
			result.addStatement("super.$L($L)", proxyMethod.simpleName(), variableNames)
					.addStatement("return");
		}

		return result.build();
	}
}
