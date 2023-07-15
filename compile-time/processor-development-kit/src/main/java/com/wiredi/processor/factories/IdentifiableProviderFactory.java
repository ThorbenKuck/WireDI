package com.wiredi.processor.factories;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.entities.AspectHandlerEntity;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.domain.entities.methods.aspecthandler.ProcessMethod;
import com.wiredi.compiler.domain.entities.methods.identifiableprovider.*;
import com.wiredi.compiler.domain.values.AspectHandlerMethod;
import com.wiredi.compiler.domain.values.FactoryMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.environment.Environment;
import com.wiredi.processor.TypeExtractor;
import com.wiredi.processor.business.IdentifiableProviderService;
import com.wiredi.processor.business.InjectionPointService;
import jakarta.inject.Inject;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class IdentifiableProviderFactory implements Factory<IdentifiableProviderEntity> {

	private static final Logger logger = Logger.get(IdentifiableProviderFactory.class);

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
	private TypeIdentifiers typeIdentifiers;

	@Inject
	private WireRepositories wireRepositories;

	@Inject
	private Environment environment;

	@Inject
	private Elements elements;

	@Override
	public IdentifiableProviderEntity create(TypeElement typeElement) {
		return create(typeElement, Annotations.getAnnotation(typeElement, Wire.class).orElseThrow());
	}

	public IdentifiableProviderEntity create(TypeElement typeElement, PropertyBinding propertyBinding) {
		return createIdentifiableProvider(typeElement)
				.appendMethod(new PrimaryMethod(false))
				.appendMethod(new SingletonMethod(true))
				.appendMethod(new GetMethod(true, typeElement.asType()))
				.appendMethod(new CreateInstanceForPropertyBindingMethod(propertyBinding, typeElement, wireRepositories, compilerRepository, environment));
	}

	public IdentifiableProviderEntity create(TypeElement typeElement, Wire annotation) {
		IdentifiableProviderEntity identifiableProviderEntity = createIdentifiableProvider(typeElement)
				.appendMethod(new PrimaryMethod(annotation.primary() || Annotations.isAnnotatedWith(typeElement, Primary.class)))
				.appendMethod(new SingletonMethod(annotation.singleton()))
				.appendMethod(new GetMethod(annotation.singleton(), typeElement.asType()))
				.appendMethod(new CreateInstanceForWireMethod(injectionPointService.injectionPoints(typeElement), wireRepositories, compilerRepository));

		identifiableProviderService.findAllFactoryMethodsIn(typeElement)
				.forEach(factoryMethod -> handleFactoryMethod(typeElement, factoryMethod));

		identifiableProviderService.findAllAspectMethods(typeElement)
				.forEach(aspectHandlerMethod -> handleAspectMethod(typeElement, aspectHandlerMethod));

		return identifiableProviderEntity;
	}

	private IdentifiableProviderEntity handleFactoryMethod(TypeElement typeElement, FactoryMethod factoryMethod) {
		var returnType = (TypeElement) types.asElement(factoryMethod.returnType());
		IdentifiableProviderEntity entity = compilerRepository.newIdentifiableProvider(providerClassName(factoryMethod), factoryMethod.returnType())
				.appendMethod(new TypeMethod(typeIdentifiers, factoryMethod.returnType()))
				.appendMethod(new AdditionalWireTypesMethod(typeExtractor.getAllSuperTypes(returnType), typeIdentifiers))
				.appendMethod(new QualifiersMethod(Qualifiers.allQualifiersOf(factoryMethod.method())))
				.appendMethod(new PrimaryMethod(Annotations.isAnnotatedWith(factoryMethod.method(), Primary.class)))
				.appendMethod(new SingletonMethod(factoryMethod.isSingleton()))
				.appendMethod(new GetMethod(factoryMethod.isSingleton(), factoryMethod.returnType()))
				.appendMethod(new CreateInstanceForFactoryMethod(factoryMethod, compilerRepository, wireRepositories, injectionPointService.injectionPoints(returnType)));

		Annotations.getAnnotation(factoryMethod.method(), Order.class)
				.or(() -> Annotations.getAnnotation(typeElement, Order.class))
				.ifPresent(order -> entity.appendMethod(new OrderMethod(order.value())));

		return entity.addSource(returnType)
				.addSource(typeElement)
				.setPackageOf(typeElement);
	}

	private AspectHandlerEntity handleAspectMethod(TypeElement typeElement, AspectHandlerMethod factoryMethod) {
		AspectHandlerEntity entity = compilerRepository.newAspectHandlerInstance(typeElement, factoryMethod.method())
				.appendMethod(new ProcessMethod(factoryMethod.method(), factoryMethod.enclosingType(), factoryMethod.annotation(), elements, types));

		Annotations.getAnnotation(factoryMethod.method(), Order.class)
				.or(() -> Annotations.getAnnotation(typeElement, Order.class))
				.ifPresent(order -> entity.appendMethod(new OrderMethod(order.value())));

		return entity.addSource(factoryMethod.enclosingType())
				.setPackageOf(typeElement);
	}

	private IdentifiableProviderEntity createIdentifiableProvider(TypeElement typeElement) {
		IdentifiableProviderEntity entity = compilerRepository.newIdentifiableProvider(typeElement)
				.appendMethod(new TypeMethod(typeIdentifiers, identifiableProviderService.getPrimaryWireType(typeElement.asType())))
				.appendMethod(new AdditionalWireTypesMethod(typeExtractor.getAllSuperTypes(typeElement), typeIdentifiers))
				.appendMethod(new QualifiersMethod(Qualifiers.allQualifiersOf(typeElement)));

		Annotations.getAnnotation(typeElement, Order.class)
				.ifPresent(order -> entity.appendMethod(new OrderMethod(order.value())));

		entity.addSource(typeElement);

		return entity;
	}

	private String providerClassName(FactoryMethod factoryMethod) {
		return types.asElement(factoryMethod.returnType()).getSimpleName()
				+ "Provider$"
				+ factoryMethod.method().getSimpleName()
				+ "$"
				+ factoryMethod.enclosingType().getSimpleName().toString();
	}
}
