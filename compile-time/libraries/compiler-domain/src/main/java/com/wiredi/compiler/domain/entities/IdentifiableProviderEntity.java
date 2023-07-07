package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.injection.*;
import com.wiredi.compiler.domain.values.FactoryMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.lang.async.FutureValue;
import com.wiredi.lang.ReflectionsHelper;
import com.wiredi.lang.SafeReference;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IdentifiableProviderEntity extends AbstractClassEntity {

	private static final Logger LOGGER = Logger.get(Logger.class);
	private static final CodeBlock EMPTY = CodeBlock.builder().build();
	private static final TypeName TYPE_IDENTIFIER_LIST = ParameterizedTypeName.get(
			ClassName.get(List.class),
			ParameterizedTypeName.get(
					ClassName.get(TypeIdentifier.class),
					WildcardTypeName.subtypeOf(Object.class)
			)
	);

	private final TypeIdentifiers typeIdentifiers;
	private final WireRepositories wireRepositories;
	private final CompilerRepository compilerRepository;

	public IdentifiableProviderEntity(TypeElement typeElement, TypeIdentifiers typeIdentifiers, WireRepositories wireRepositories, CompilerRepository compilerRepository) {
		this(typeElement.asType(), typeElement.getSimpleName().toString() + "IdentifiableProvider", typeIdentifiers, wireRepositories, compilerRepository);
	}

	public IdentifiableProviderEntity(TypeMirror element, String name, TypeIdentifiers typeIdentifiers, WireRepositories wireRepositories, CompilerRepository compilerRepository) {
		super(element, name);
		this.typeIdentifiers = typeIdentifiers;
		this.wireRepositories = wireRepositories;
		this.compilerRepository = compilerRepository;
	}

	@Override
	protected TypeSpec.Builder createBuilder(TypeMirror type) {
		return TypeSpec.classBuilder(className)
				.addSuperinterface(
						ParameterizedTypeName.get(
								ClassName.get(IdentifiableProvider.class),
								TypeName.get(type)
						)
				).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
	}

	@Override
	@NotNull
	public List<Class<?>> autoServiceTypes() {
		return List.of(IdentifiableProvider.class);
	}

	public IdentifiableProviderEntity appendCreateInstanceMethod(
			InjectionPoints injectionPoints
	) {
		VariableContext variableContext = new VariableContext();

		builder.addMethod(
				MethodSpec.methodBuilder("createInstance")
						.returns(TypeName.get(rootType()))
						.addModifiers(Modifier.PRIVATE)
						.addParameter(WireRepository.class, "wireRepository", Modifier.FINAL)
						.addCode(constructorInvocationStep(injectionPoints.constructorInjectionPoint(), variableContext))
						.addCode(fieldInjectionStep(injectionPoints.fieldInjections(), variableContext))
						.addCode(methodInjectionStep(injectionPoints.methodInjections(), variableContext))
						.addCode(postConstruct(injectionPoints.postConstructInjectionPoints(), variableContext))
						.addStatement("return instance")
						.build()
		);

		return this;
	}

	private CodeBlock constructorInvocationStep(
			@Nullable ConstructorInjectionPoint constructorInjectionPoint,
			@NotNull VariableContext context
	) {
		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
		List<String> parameters = new ArrayList<>();

		if (constructorInjectionPoint != null) {
			int parameterCount = constructorInjectionPoint.constructor().getParameters().size();
			if (parameterCount > 0) {
				codeBlockBuilder.add("// We will start by Fetching all $L constructor parameters\n", parameterCount);
				constructorInjectionPoint.constructor().getParameters().forEach(parameter -> {
					TypeName typeName = TypeName.get(parameter.asType());
					String varName = context.instantiateVariableIfRequired(Qualifiers.injectionQualifier(parameter), parameter.asType(), (name, qualifier) -> {
						codeBlockBuilder.addStatement("$T $L = $L", typeName, name, wireRepositories.fetchFromWireRepository(parameter.asType(), qualifier));
					});
					parameters.add(varName);
				});
			}
		}

		String parameterNames = String.join(",", parameters);
		return codeBlockBuilder.addStatement("$T instance = new $T($L)", rootType(), rootType(), parameterNames).build();
	}

	private CodeBlock fieldInjectionStep(List<? extends FieldInjectionPoint> fieldInjections, VariableContext variableContext) {
		if (fieldInjections.isEmpty()) {
			return EMPTY;
		}

		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("\n// Field injections\n");

		fieldInjections.forEach(injectionPoint -> {
			if (injectionPoint.isPackagePrivate() && !willHaveTheSamePackageAs(injectionPoint.field())) {
				WireBridgeEntity bridge = compilerRepository.newWireBridgeEntity(className, injectionPoint.getDeclaringClass());
				codeBlockBuilder.add("$T", bridge.className())
						.add(".$L", bridge.bridgePackagePrivateField(injectionPoint));
				codeBlockBuilder.addStatement("(wireRepository, instance)");
			} else {
				String getValue = variableContext.instantiateVariableIfRequired(Qualifiers.injectionQualifier(injectionPoint.field()), injectionPoint.type(), (name, qualifierType) -> {
					codeBlockBuilder.addStatement("$T $L = $L", injectionPoint.type(), name, wireRepositories.fetchFromWireRepository(injectionPoint.field(), qualifierType));
				});

				if (injectionPoint.requiresReflection() || requiresReflectionFor(injectionPoint.field())) {
					LOGGER.reflectionWarning(injectionPoint.field());
					codeBlockBuilder.add("// This Field requires reflections. If you are reading this think about make this field package private or protected instead\n");
					codeBlockBuilder.add("$T.setField(", ReflectionsHelper.class)
							.add("$S, ", injectionPoint.name())
							.add("$L, ", "instance")
							.add("$T.class, ", injectionPoint.getDeclaringClass())
							.addStatement("$L)", getValue);
				} else {
					codeBlockBuilder.addStatement("$L.$L = $L", "instance", injectionPoint.name(), getValue);
				}
			}
		});
		return codeBlockBuilder.build();
	}

	private boolean requiresReflectionFor(ExecutableElement element) {
		if (element.getModifiers().contains(Modifier.PUBLIC)) {
			return false;
		}

		return packageElement().map(it -> it.equals(packageElementOf(element))).orElse(true);
	}

	private boolean requiresReflectionFor(VariableElement element) {
		if (element.getModifiers().contains(Modifier.PUBLIC)) {
			return false;
		}

		return packageElement().map(it -> it.equals(packageElementOf(element))).orElse(true);
	}

	private CodeBlock methodInjectionStep(List<? extends MethodInjectionPoint> methodInjections, VariableContext variableContext) {
		if (methodInjections.isEmpty()) {
			return EMPTY;
		}

		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("\n// Method Injections\n");
		methodInjections.forEach(injectionPoint -> codeBlockBuilder.add(callMethod(injectionPoint, variableContext)));
		return codeBlockBuilder.build();
	}

	private CodeBlock postConstruct(List<? extends PostConstructInjectionPoint> injectionPoints, VariableContext variableContext) {
		if (injectionPoints.isEmpty()) {
			return EMPTY;
		}

		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder().add("\n// Now let us inform the class about its construction\n");
		injectionPoints.forEach(injectionPoint -> {
			if (injectionPoint.supportsAsyncInvocation()) {
				codeBlockBuilder.add("$T.run(() -> {\n", ParameterizedTypeName.get(FutureValue.class)).indent();
			}
			codeBlockBuilder.add(callMethod(injectionPoint, variableContext));
			if (injectionPoint.supportsAsyncInvocation()) {
				codeBlockBuilder.unindent().add("});\n");
			}
		});


		// TODO Add PostConstruct method invocation
		return codeBlockBuilder.build();
	}

	public IdentifiableProviderEntity isPrimary(boolean primary) {
		if (primary) {
			builder.addMethod(
					MethodSpec.methodBuilder("primary")
							.returns(ClassName.BOOLEAN)
							.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
							.addStatement("return true")
							.addAnnotation(Override.class)
							.build()
			);
		}

		return this;
	}

	public IdentifiableProviderEntity isSingleton(boolean singleton) {
		return isSingleton(singleton, rootType());
	}

	public IdentifiableProviderEntity isSingleton(boolean singleton, TypeMirror returnType) {
		builder.addMethod(
				MethodSpec.methodBuilder("isSingleton")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(Override.class)
						.addStatement("return $L", singleton)
						.returns(TypeName.BOOLEAN)
						.build()
		);
		List<Modifier> getMethodModifier = new ArrayList<>(List.of(Modifier.PUBLIC, Modifier.FINAL));

		CodeBlock.Builder getCodeBlock = CodeBlock.builder();
		if (singleton) {
			getCodeBlock.addStatement("return instance.getOrSet(() -> createInstance(wireRepository))");

			builder.addField(
					FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(SafeReference.class), TypeName.get(rootType())), "instance")
							.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
							.initializer("$T.empty()", SafeReference.class)
							.build()
			);
			getMethodModifier.add(Modifier.SYNCHRONIZED);
		} else {
			getCodeBlock.addStatement("return createInstance(wireRepository)");
		}

		builder.addMethod(
				MethodSpec.methodBuilder("get")
						.addModifiers(getMethodModifier)
						.addAnnotation(Override.class)
						.addParameter(
								ParameterSpec.builder(WireRepository.class, "wireRepository", Modifier.FINAL)
										.addAnnotation(NotNull.class)
										.build()
						).addCode(getCodeBlock.build())
						.returns(TypeName.get(returnType))
						.build());

		return this;
	}

	public IdentifiableProviderEntity setPrimaryWireType(TypeMirror type) {
		String variableName = "PRIMARY_WIRE_TYPE";
		ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(TypeIdentifier.class), TypeName.get(type));

		builder.addField(
				FieldSpec.builder(typeName, variableName)
						.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
						.initializer(typeIdentifiers.newTypeIdentifier(type)).build()
		).addMethod(
				MethodSpec.methodBuilder("type")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(Override.class)
						.addAnnotation(NotNull.class)
						.returns(typeName)
						.addStatement("return $L", variableName)
						.build()
		);

		return this;
	}

	public IdentifiableProviderEntity setAdditionalWireTypes(List<TypeMirror> typeElements) {
		List<CodeBlock> typeIdentifier = new ArrayList<>();

		typeElements.stream()
				.filter(Objects::nonNull)
				.filter(it -> it.getKind() != TypeKind.NONE)
				.map(typeIdentifiers::newTypeIdentifier)
				.forEach(typeIdentifier::add);

		if (typeIdentifier.isEmpty()) {
			return this;
		}

		builder.addField(
				FieldSpec.builder(TYPE_IDENTIFIER_LIST, "ADDITIONAL_WIRE_TYPES")
						.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
						.initializer(
								CodeBlock.builder()
										.add("$T.of(\n", List.class)
										.indent()
										.add(CodeBlock.join(typeIdentifier, ",\n"))
										.unindent()
										.add("\n)")
										.build()
						)
						.build()
		).addMethod(
				MethodSpec.methodBuilder("additionalWireTypes")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.returns(TYPE_IDENTIFIER_LIST)
						.addAnnotation(Override.class)
						.addAnnotation(NotNull.class)
						.addStatement("return ADDITIONAL_WIRE_TYPES")
						.build()
		);

		return this;
	}

	private String getVariablesFromWireRepository(CodeBlock.Builder rootCodeBlock, List<? extends VariableElement> parameters, VariableContext variableContext) {
		List<String> fetchVariables = new ArrayList<>();

		parameters.forEach(parameter -> {
			String variableName = variableContext.instantiateVariableIfRequired(Qualifiers.injectionQualifier(parameter), parameter.asType(), (name, qualifierType) -> {
				rootCodeBlock.addStatement("$T $L = $L", parameter.asType(), name, wireRepositories.fetchFromWireRepository(parameter.asType(), qualifierType));
			});
			fetchVariables.add(variableName);
		});

		return String.join(", ", fetchVariables);
	}

	public IdentifiableProviderEntity order(int order) {
		builder.addMethod(
				MethodSpec.methodBuilder("getOrder")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.returns(TypeName.INT)
						.addAnnotation(Override.class)
						.addStatement("return $L", order)
						.build()
		);
		return this;
	}

	public IdentifiableProviderEntity appendProviderFunction(
			FactoryMethod factoryMethod,
			InjectionPoints injectionPoints
	) {
		final VariableContext variableContext = new VariableContext();
		final CodeBlock.Builder methodBody = CodeBlock.builder()
				.add("$T builder = ", factoryMethod.enclosingType().asType())
				.addStatement("$L", wireRepositories.fetchFromWireRepository(factoryMethod.enclosingType()));

		final List<String> params = new ArrayList<>();
		factoryMethod.method()
				.getParameters()
				.forEach(parameter -> {
					String variableName = variableContext.instantiateVariableIfRequired(parameter, (name, qualifier) -> {
						methodBody.addStatement("$T $L = $L", parameter.asType(), name, wireRepositories.fetchFromWireRepository(parameter.asType(), qualifier));
					});
					params.add(variableName);
				});

		methodBody.addStatement("$T instance = builder.$L($L)", rootType(), factoryMethod.name(), String.join(", ", params));

		builder.addMethod(
				MethodSpec.methodBuilder("createInstance")
						.returns(TypeName.get(factoryMethod.returnType()))
						.addModifiers(Modifier.PRIVATE)
						.addParameter(WireRepository.class, "wireRepository", Modifier.FINAL)
						.addCode(methodBody.build())
						.addCode(fieldInjectionStep(injectionPoints.fieldInjections(), variableContext))
						.addCode(methodInjectionStep(injectionPoints.methodInjections(), variableContext))
						.addCode(postConstruct(injectionPoints.postConstructInjectionPoints(), variableContext))
						.addStatement("return instance")
						.build()
		);

		return this;
	}

	private CodeBlock callMethod(MethodInjectionPoint injectionPoint, VariableContext variableContext) {
		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
		if (injectionPoint.isPackagePrivate() && !willHaveTheSamePackageAs(injectionPoint.method())) {
			WireBridgeEntity bridge = compilerRepository.newWireBridgeEntity(className, injectionPoint.getDeclaringClass());
			codeBlockBuilder.add("$T", bridge.className())
					.add(".$L", bridge.bridgePackagePrivateMethod(injectionPoint));
			codeBlockBuilder.addStatement("(wireRepository, instance)");
		} else {
			String fetchVariables = getVariablesFromWireRepository(codeBlockBuilder, injectionPoint.parameters(), variableContext);

			if (injectionPoint.requiresReflection() || requiresReflectionFor(injectionPoint.method())) {
				LOGGER.reflectionWarning(injectionPoint.method());
				codeBlockBuilder.add("// This function requires reflections. If you are reading this think about make this function package private or protected instead\n");
				codeBlockBuilder.add("$T.invokeMethod(", ReflectionsHelper.class)
						.add("$L, ", "instance")
						.add("$T.class, ", injectionPoint.getDeclaringClass())
						.add("$S, ", injectionPoint.name())
						.add("$T.class", injectionPoint.returnValue());
				if (!fetchVariables.isBlank()) {
					codeBlockBuilder.add(", $L", fetchVariables);
				}
				codeBlockBuilder.addStatement(")");
			} else {
				codeBlockBuilder.addStatement("$L.$L($L)", "instance", injectionPoint.name(), fetchVariables);
			}
		}
		return codeBlockBuilder.build();
	}

	private CodeBlock qualifierValueBuilder(QualifierType qualifier) {
		if (qualifier.values().isEmpty()) {
			return CodeBlock.builder().add("$T.just($S)", QualifierType.class, qualifier.name()).build();
		} else {
			CodeBlock.Builder builder = CodeBlock.builder()
					.add("$T.newInstance($S)", QualifierType.class, qualifier.name())
					.indent();
			qualifier.forEach((key, value) -> {
				builder.add("\n.add($S, ", key);
				if (value instanceof String) {
					builder.add("$S", value);
				} else if (value instanceof Character) {
					builder.add("'$L'", value);
				} else if (value instanceof Class<?> c) {
					builder.add("$S", c.getName());
				} else {
					builder.add("$L", value);
				}
				builder.add(")");
			});
			return builder.add("\n.build()").unindent().build();
		}
	}

	public IdentifiableProviderEntity setQualifiers(List<QualifierType> qualifierTypes) {
		if (qualifierTypes.isEmpty()) {
			return this;
		}
		final String fieldName = "QUALIFIER";
		List<CodeBlock> values = qualifierTypes.stream()
				.map(this::qualifierValueBuilder)
				.toList();

		builder.addField(
				FieldSpec.builder(ParameterizedTypeName.get(List.class, QualifierType.class), fieldName)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.addAnnotation(NotNull.class)
						.initializer(
								CodeBlock.builder()
										.add("$T.of(\n", List.class).indent()
										.add(CodeBlock.join(values, ",\n"))
										.unindent().add("\n)")
										.build()
						).build()
		).addMethod(
				MethodSpec.methodBuilder("qualifiers")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(NotNull.class)
						.addAnnotation(Override.class)
						.returns(ParameterizedTypeName.get(List.class, QualifierType.class))
						.addStatement("return $L", fieldName)
						.build()
		);

		return this;
	}
}
