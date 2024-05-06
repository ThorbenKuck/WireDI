package com.wiredi.compiler.processor.factories;

import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.compiler.domain.entities.AspectAwareProxyEntity;
import com.wiredi.compiler.domain.injection.NameContext;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.processor.TypeExtractor;
import com.wiredi.compiler.processor.business.AspectAwareProxyService;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.List;

public class AspectAwareProxyFactory implements Factory<AspectAwareProxyEntity> {

	private final CompilerRepository compilerRepository;
	private final TypeExtractor typeExtractor;
	private final AspectAwareProxyService proxyService;
	private final Types types;
	private static final Logger logger = Logger.get(AspectAwareProxyFactory.class);

	public AspectAwareProxyFactory(
            CompilerRepository compilerRepository,
            TypeExtractor typeExtractor,
            AspectAwareProxyService proxyService, Types types
    ) {
		this.compilerRepository = compilerRepository;
		this.typeExtractor = typeExtractor;
		this.proxyService = proxyService;
        this.types = types;
    }

	@Override
	public AspectAwareProxyEntity create(TypeElement typeElement) {
		List<ProxyMethod> eligibleMethods = proxyService.findEligibleMethods(typeElement);
		if (eligibleMethods.isEmpty()) {
			return null;
		}
		logger.info("Will create an aspect proxy for " + typeElement);

		final AspectAwareProxyEntity entity = compilerRepository.newAspectAwareProxy(typeElement);
		final NameContext nameContext = new NameContext();

		eligibleMethods.forEach(proxyMethod -> entity.proxyMethod(proxyMethod, nameContext));
		entity.addWiredAnnotationFor(typeExtractor.getAllSuperTypes(typeElement).stream().map(types::erasure).toList())
				.addConstructorInvocation(TypeUtils.findPrimaryConstructor(typeElement).orElse(null));

		return entity;
	}

}
