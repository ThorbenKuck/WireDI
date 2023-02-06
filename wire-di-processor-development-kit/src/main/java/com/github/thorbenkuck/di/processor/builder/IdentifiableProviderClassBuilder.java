package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.domain.provider.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.provider.TypeIdentifier;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.constructors.MethodConstructor;
import com.github.thorbenkuck.di.processor.builder.constructors.factory.CreateInstanceForPropertySourceMethodConstructor;
import com.github.thorbenkuck.di.processor.builder.constructors.factory.CreateInstanceForProviderMethodConstructor;
import com.github.thorbenkuck.di.processor.builder.constructors.factory.CreateInstanceForWireMethodConstructor;
import com.github.thorbenkuck.di.runtime.WireRepository;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.lang.invoke.TypeDescriptor;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class IdentifiableProviderClassBuilder extends ClassBuilder {

	private final WireInformation wireInformation;

	private static final ClassName PROVIDER_CLASS_NAME = ClassName.get(IdentifiableProvider.class);
	private static final AnnotationSpec AUTO_SERVICE_ANNOTATION_SPEC = AnnotationSpec.builder(AutoService.class)
			.addMember("value", "$T.class", IdentifiableProvider.class)
			.build();

	public IdentifiableProviderClassBuilder(WireInformation wireInformation) {
		super(wireInformation);
		this.wireInformation = wireInformation;
		setup();
	}

	@Override
	protected TypeSpec.Builder initialize() {
		return TypeSpec.classBuilder(wireInformation.simpleClassName() + "IdentifiableProvider")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addSuperinterface(ParameterizedTypeName.get(PROVIDER_CLASS_NAME, wireInformation.primaryClassName()))
				.addAnnotation(AUTO_SERVICE_ANNOTATION_SPEC);
	}

	public IdentifiableProviderClassBuilder overwriteAllRequiredMethods() {
		return addPriorityMethod()
				.addSingletonMethod()
				.addTypeMethod()
				.addWiredTypesMethod()
				.addGetMethod();
	}

	public IdentifiableProviderClassBuilder addPriorityMethod() {
		wireInformation.getWirePriority().ifPresent(priority -> {
			addMethod(
					overwriteMethod("priority")
							.returns(int.class)
							.addCode(CodeBlock.builder().addStatement("return $L", priority).build())
			);

		});

		return this;
	}

	public IdentifiableProviderClassBuilder addSingletonMethod() {
		boolean singleton = wireInformation.isSingleton();
		addMethod(
				overwriteMethod("isSingleton")
						.returns(TypeName.BOOLEAN)
						.addCode(CodeBlock.builder().addStatement("return $L", singleton).build())
		);

		return this;
	}

	public IdentifiableProviderClassBuilder addTypeMethod() {
		TypeElement primaryWireType = wireInformation.getPrimaryWireType();
		String fieldName = "PRIMARY_WIRE_TYPE";
		addMethod(
				overwriteMethod("type")
						.addAnnotation(NotNull.class)
						.returns(ClassName.get(Class.class))
						.addStatement("return $L", fieldName)
		);

		addField(
				classConstant(genericClass(primaryWireType), fieldName)
						.initializer("$T.class", ClassName.get(primaryWireType))
		);

		return this;
	}

	public IdentifiableProviderClassBuilder addWiredTypesMethod() {
		final String lineSeparator = System.lineSeparator();
		final CodeBlock.Builder initializer = CodeBlock.builder()
				.add("new $T[] {", TypeIdentifier.class)
				.add(lineSeparator);
		final AtomicBoolean first = new AtomicBoolean(true);
		final String fieldName = "ALL_WIRED_TYPES";
		List<TypeElement> allWireCandidates = wireInformation.getAllWireCandidates();

		if(!allWireCandidates.isEmpty()) {
			initializer.indent();
			allWireCandidates
					.forEach(it -> {
						if (first.get()) {
							initializer.add("$T.of($T.class)", TypeIdentifier.class, ClassName.get(it));
							first.set(false);
						} else {
							initializer.add(",").add(lineSeparator).add("$T.of($T.class)", TypeIdentifier.class, ClassName.get(it));
						}
					});
			initializer.add(lineSeparator).unindent();
		}

		addField(
				classConstant(TypeName.get(TypeIdentifier[].class), fieldName)
						.initializer(initializer.add("}").build())
		);

		addMethod(
				overwriteMethod("wiredTypes")
						.addStatement("return $L", fieldName)
						.returns(TypeName.get(TypeIdentifier[].class))
		);

		return this;
	}

	public IdentifiableProviderClassBuilder addGetMethod() {
		final String fieldName = "instance";
		MethodSpec.Builder getMethodBuilder = overwriteMethod("get")
				.addParameter(
                        ParameterSpec.builder(ClassName.get(WireRepository.class), "wireRepository", Modifier.FINAL)
                                .addAnnotation(NotNull.class)
                                .build()
                )
				.returns(TypeName.get(wireInformation.getPrimaryWireType().asType()));

		if (wireInformation.isSingleton()) {
			addField(
					FieldSpec.builder(TypeName.get(wireInformation.getPrimaryWireType().asType()), fieldName)
							.addModifiers(Modifier.VOLATILE)
							.addModifiers(Modifier.PRIVATE)
			);

			getMethodBuilder.addModifiers(Modifier.SYNCHRONIZED)
					.beginControlFlow("if(this.instance == null)")
					.addStatement("this.instance = createInstance(wireRepository)")
					.endControlFlow()
					.addStatement("return instance");
		} else {
			getMethodBuilder.addStatement("return createInstance(wireRepository)");
		}

		addMethod(getMethodBuilder);
		return this;
	}

	public IdentifiableProviderClassBuilder applyMethodBuilder(MethodConstructor methodConstructor) {
		methodConstructor.construct(wireInformation, classBuilder());

		return this;
	}

	public IdentifiableProviderClassBuilder identifyingAWiredSource() {
		return applyMethodBuilder(new CreateInstanceForWireMethodConstructor());
	}

	public IdentifiableProviderClassBuilder identifyingAPropertySource() {
		return applyMethodBuilder(new CreateInstanceForPropertySourceMethodConstructor());
	}

	public IdentifiableProviderClassBuilder identifyingAProviderSource() {
		return applyMethodBuilder(new CreateInstanceForProviderMethodConstructor());
	}
}
