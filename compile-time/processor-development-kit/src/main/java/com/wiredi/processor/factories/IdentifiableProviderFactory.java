package com.wiredi.processor.factories;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.domain.values.FactoryMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.processor.TypeExtractor;
import com.wiredi.processor.business.IdentifiableProviderService;
import com.wiredi.processor.business.InjectionPointService;
import jakarta.inject.Inject;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class IdentifiableProviderFactory implements Factory<IdentifiableProviderEntity> {

	private static final Logger logger = Logger.get(IdentifiableProviderEntity.class);
	@Inject
	private CompilerRepository compilerRepository;
	@Inject
	private IdentifiableProviderService identifiableProviderService;
	@Inject
	private InjectionPointService injectionPointService;
	@Inject
	private TypeExtractor typeExtractor;
	@Inject
	private Types types;
	@Inject
	private Elements elements;

	@Override
	public IdentifiableProviderEntity create(TypeElement typeElement) {
		return create(typeElement, Annotations.getAnnotation(typeElement, Wire.class).orElseThrow());
	}

	public IdentifiableProviderEntity create(TypeElement typeElement, Wire annotation) {
		IdentifiableProviderEntity identifiableProviderEntity = createIdentifiableProvider(typeElement, annotation);
		Annotations.getAnnotation(typeElement, Order.class)
				.ifPresent(order -> identifiableProviderEntity.order(order.value()));

		identifiableProviderEntity.addSource(typeElement);

		identifiableProviderService.findAllFactoryMethodsIn(typeElement)
				.parallelStream()
				.forEach(factoryMethod -> handleFactoryMethod(typeElement, factoryMethod)
						.addSource(typeElement));

		return identifiableProviderEntity;
	}

	private AbstractClassEntity handleFactoryMethod(TypeElement typeElement, FactoryMethod factoryMethod) {
		var returnType = (TypeElement) types.asElement(factoryMethod.returnType());
		IdentifiableProviderEntity entity = compilerRepository.newIdentifiableProvider(providerClassName(factoryMethod), factoryMethod.returnType())
				.setPrimaryWireType(factoryMethod.returnType())
				.setAdditionalWireTypes(typeExtractor.getAllSuperTypes(returnType))
				.isPrimary(Annotations.isAnnotatedWith(factoryMethod.method(), Primary.class))
				.isSingleton(factoryMethod.isSingleton(), factoryMethod.returnType())
				.setQualifiers(Qualifiers.allQualifiersOf(factoryMethod.method()))
				.appendProviderFunction(factoryMethod, injectionPointService.injectionPoints(returnType));

		Annotations.getAnnotation(factoryMethod.method(), Order.class)
				.or(() -> Annotations.getAnnotation(typeElement, Order.class))
				.ifPresent(order -> entity.order(order.value()));

		return entity.addSource(returnType)
				.setPackageOf(typeElement);
	}

	private IdentifiableProviderEntity createIdentifiableProvider(TypeElement typeElement, Wire annotation) {
		return compilerRepository.newIdentifiableProvider(typeElement)
				.setPrimaryWireType(identifiableProviderService.getPrimaryWireType(typeElement.asType()))
				.setAdditionalWireTypes(typeExtractor.getAllSuperTypes(typeElement))
				.isPrimary(annotation.primary() || Annotations.isAnnotatedWith(typeElement, Primary.class))
				.isSingleton(annotation.singleton())
				.setQualifiers(Qualifiers.allQualifiersOf(typeElement))
				.appendCreateInstanceMethod(injectionPointService.injectionPoints(typeElement));

	}

	private String providerClassName(FactoryMethod factoryMethod) {
		return types.asElement(factoryMethod.returnType()).getSimpleName()
				+ "Provider$"
				+ factoryMethod.method().getSimpleName()
				+ "$"
				+ factoryMethod.enclosingType().getSimpleName().toString();
	}
}
