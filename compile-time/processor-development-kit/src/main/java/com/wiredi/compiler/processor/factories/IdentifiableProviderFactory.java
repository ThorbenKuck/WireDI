package com.wiredi.compiler.processor.factories;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.compiler.domain.annotations.TypedAnnotationSearch;
import com.wiredi.compiler.domain.entities.AspectHandlerEntity;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.domain.entities.methods.aspecthandler.AppliesToMethod;
import com.wiredi.compiler.domain.entities.methods.aspecthandler.ProcessMethod;
import com.wiredi.compiler.domain.entities.methods.identifiableprovider.*;
import com.wiredi.compiler.domain.properties.PropertyContext;
import com.wiredi.compiler.domain.values.AspectHandlerMethod;
import com.wiredi.compiler.domain.values.FactoryMethod;
import com.wiredi.runtime.lang.Ordered;
import org.slf4j.Logger;import com.wiredi.compiler.processor.TypeExtractor;
import com.wiredi.compiler.processor.business.IdentifiableProviderService;
import com.wiredi.compiler.processor.business.InjectionPointService;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.Environment;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Optional;

public class IdentifiableProviderFactory implements Factory<IdentifiableProviderEntity> {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(IdentifiableProviderFactory.class);

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

    @Inject
    private PropertyContext propertyContext;

    private final TypedAnnotationSearch<Primary> primaryAnnotationSearch = Annotations.search().byType(Primary.class);
    private final TypedAnnotationSearch<Wire> wireAnnotationSearch = Annotations.search().byType(Wire.class);
    private final TypedAnnotationSearch<Order> orderAnnotationSearch = Annotations.search().byType(Order.class);

    @Override
    public IdentifiableProviderEntity create(TypeElement typeElement) {
        return create(typeElement, wireAnnotationSearch.findFirstIn(typeElement).orElseThrow());
    }

    public IdentifiableProviderEntity create(TypeElement typeElement, PropertyBinding propertyBinding) {
        return createIdentifiableProvider(typeElement)
                .addMethod(new PrimaryMethod(false))
                .addMethod(new GetMethod(typeElement.asType()))
                .addMethod(new CreateInstanceForPropertyBindingMethod(propertyBinding, typeElement, wireRepositories, compilerRepository, environment, propertyContext));
    }

    public IdentifiableProviderEntity create(TypeElement typeElement, @Nullable Wire annotation) {
        IdentifiableProviderEntity identifiableProviderEntity = createIdentifiableProvider(typeElement)
                .addMethod(new PrimaryMethod(Optional.ofNullable(annotation).map(Wire::primary).orElse(false) || primaryAnnotationSearch.isPresentIn(typeElement)))
                .addMethod(new GetMethod(typeElement.asType()))
                .addMethod(new CreateInstanceForWireMethod(injectionPointService.injectionPoints(typeElement), wireRepositories, compilerRepository));

        identifiableProviderService.findAllFactoryMethodsIn(typeElement)
                .forEach(factoryMethod -> {
                    IdentifiableProviderEntity entity = handleFactoryMethod(typeElement, factoryMethod);
                    identifiableProviderEntity.addChild(entity);
                });

        identifiableProviderService.findAllAspectMethods(typeElement)
                .forEach(aspectHandlerMethod -> handleAspectMethod(typeElement, aspectHandlerMethod));

        return identifiableProviderEntity;
    }

    private IdentifiableProviderEntity handleFactoryMethod(TypeElement typeElement, FactoryMethod factoryMethod) {
        var returnType = (TypeElement) types.asElement(factoryMethod.returnType());

        logger.debug("Creating IdentifiableProvider for type {}", returnType);
        IdentifiableProviderEntity entity = compilerRepository.newIdentifiableProvider(factoryMethod.method(), providerClassName(factoryMethod), factoryMethod.returnType())
                .addMethod(new TypeMethod(typeIdentifiers, factoryMethod.returnType()))
                .addMethod(new PrimaryMethod(primaryAnnotationSearch.isPresentIn(factoryMethod.method())))
                .addMethod(new GetMethod(factoryMethod.returnType()))
                .addMethod(new CreateInstanceForFactoryMethod(factoryMethod, compilerRepository, wireRepositories, injectionPointService.injectionPoints(returnType)));

        if(factoryMethod.superTypes() == Provider.SuperTypes.ALL) {
            entity.addMethod(new AdditionalWireTypesMethod(typeExtractor.getAllSuperTypes(returnType), typeIdentifiers));
        } else if (factoryMethod.superTypes() == Provider.SuperTypes.DECLARED) {
            entity.addMethod(new AdditionalWireTypesMethod(typeExtractor.getAdditionalWireTypesOf(returnType), typeIdentifiers));
        }

        Optional<Order> orderAnnotation = orderAnnotationSearch.findFirstIn(typeElement);
        if (orderAnnotation.isPresent()) {
            entity.addMethod(new OrderMethod(orderAnnotation.get(), + 1, types));
        } else {
            entity.addMethod(new OrderMethod(Ordered.DEFAULT + 1));
        }

        return entity.addSource(returnType)
                .addSource(typeElement)
                .setPackageOf(typeElement);
    }

    private AspectHandlerEntity handleAspectMethod(TypeElement typeElement, AspectHandlerMethod factoryMethod) {
        AspectHandlerEntity entity = compilerRepository.newAspectHandlerInstance(typeElement, factoryMethod.method())
                .addMethod(new ProcessMethod(factoryMethod.method(), factoryMethod.enclosingType(), factoryMethod.annotation(), elements, types))
                .addMethod(new AppliesToMethod(factoryMethod));

        orderAnnotationSearch.findFirstIn(factoryMethod.method())
                .or(() -> orderAnnotationSearch.findFirstIn(typeElement))
                .ifPresent(order -> entity.addMethod(new OrderMethod(order, types)));

        return entity.addSource(factoryMethod.enclosingType())
                .setPackageOf(typeElement);
    }

    private IdentifiableProviderEntity createIdentifiableProvider(TypeElement typeElement) {
        IdentifiableProviderEntity entity = compilerRepository.newIdentifiableProvider(typeElement)
                .addMethod(new TypeMethod(typeIdentifiers, identifiableProviderService.getPrimaryWireType(typeElement.asType())))
                .addMethod(new AdditionalWireTypesMethod(typeExtractor.getAdditionalWireTypesOf(typeElement), typeIdentifiers));

        orderAnnotationSearch.findFirstIn(typeElement)
                .ifPresent(order -> entity.addMethod(new OrderMethod(order, types)));

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
