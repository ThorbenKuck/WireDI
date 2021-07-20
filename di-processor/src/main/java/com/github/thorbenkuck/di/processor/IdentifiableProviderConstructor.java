package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.IdentifiableProvider;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.processor.constructors.*;
import com.github.thorbenkuck.di.processor.foundation.Logger;
import com.github.thorbenkuck.di.processor.wire.ConstructorFinder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.PostConstruct;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class IdentifiableProviderConstructor {

	private final TypeElement typeElement;
	private final List<Consumer<TypeSpec.Builder>> consumers = new ArrayList<>();
	private final List<TypeMirror> wireTo = new ArrayList<>();

	private final List<MethodConstructor> constructorList;

	public void addConsumer(Consumer<TypeSpec.Builder> consumer) {
		consumers.add(consumer);
	}

	public IdentifiableProviderConstructor(TypeElement typeElement, Types types, Logger logger) {
		this.typeElement = typeElement;

		constructorList = Arrays.asList(
				new GetAndLazyMethodConstructor(),
				new ConstructorFinder(logger),
				new TypeIdentifierConstructor(types, logger),
				new OrderMethodConstructor(),
				new SingletonMethodConstructor()
		);
	}

	public TypeSpec construct() {
		TypeSpec.Builder builder = TypeSpec.classBuilder(typeElement.getSimpleName() + "IdentifiableProvider")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IdentifiableProvider.class), TypeName.get(typeElement.asType())));

		builder.addAnnotation(AnnotationSpec.builder(AutoService.class)
				.addMember("value", "$T.class", IdentifiableProvider.class)
				.build());

		constructorList.forEach(constructor -> constructor.construct(typeElement, builder));

		for(Consumer<TypeSpec.Builder> consumer : consumers) {
			consumer.accept(builder);
		}

		return builder.build();
	}

	public static IdentifiableProviderBuilder build(TypeElement typeElement, Types types, Logger logger) {
		return new IdentifiableProviderBuilder(new IdentifiableProviderConstructor(typeElement, types, logger));
	}

	public static class IdentifiableProviderBuilder {

		private final IdentifiableProviderConstructor constructor;

		public IdentifiableProviderBuilder(IdentifiableProviderConstructor constructor) {
			this.constructor = constructor;
		}

		public IdentifiableProviderBuilder basedOnWireAnnotation(Wire wire) {
			try {
				wire.to();
			} catch (MirroredTypesException e) {
				constructor.wireTo.addAll(e.getTypeMirrors());
			}

			return this;
		}

		public IdentifiableProviderBuilder addIdentifyingType(TypeElement typeElement) {
			return addIdentifyingType(typeElement.asType());
		}

		public IdentifiableProviderBuilder addIdentifyingType(TypeMirror typeMirror) {
			constructor.wireTo.add(typeMirror);

			return this;
		}

		public TypeSpec build() {
			return constructor.construct();
		}

	}
}
