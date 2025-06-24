package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IdentifiableProviderEntity extends AbstractClassEntity<IdentifiableProviderEntity> {

	private final List<IdentifiableProviderEntity> children = new ArrayList<>();

	public IdentifiableProviderEntity(TypeElement typeElement, Annotations annotations) {
		this(typeElement, typeElement.asType(), typeElement.getSimpleName().toString() + "IdentifiableProvider", annotations);
	}

	public IdentifiableProviderEntity(Element source, TypeMirror element, String name, Annotations annotations) {
		super(source, element, name, annotations);
	}

	public void addChild(IdentifiableProviderEntity provider) {
		this.children.add(provider);
	}

	public void onChildren(Consumer<IdentifiableProviderEntity> consumer) {
		children.forEach(child -> {
			consumer.accept(child);
			child.onChildren(consumer);
		});
	}

	@Override
	protected TypeSpec.Builder createBuilder(TypeMirror type) {
		return TypeSpec.classBuilder(className())
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
}
