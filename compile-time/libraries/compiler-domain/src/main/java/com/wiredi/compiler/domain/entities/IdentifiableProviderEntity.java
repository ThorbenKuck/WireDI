package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.*;
import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
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

public class IdentifiableProviderEntity extends AbstractClassEntity<IdentifiableProviderEntity> {

	public IdentifiableProviderEntity(TypeElement typeElement) {
		this(typeElement.asType(), typeElement.getSimpleName().toString() + "IdentifiableProvider");
	}

	public IdentifiableProviderEntity(TypeMirror element, String name) {
		super(element, name);
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

	public IdentifiableProviderEntity appendMethod(MethodFactory methodFactory) {
		methodFactory.append(builder, this);
		return this;
	}
}
