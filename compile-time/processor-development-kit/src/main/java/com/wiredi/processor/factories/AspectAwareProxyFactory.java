package com.wiredi.processor.factories;

import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.compiler.domain.entities.AspectAwareProxyEntity;
import com.wiredi.compiler.domain.injection.NameContext;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.processor.TypeExtractor;
import com.wiredi.processor.business.AspectAwareProxyService;

import javax.lang.model.element.TypeElement;
import java.util.List;

public class AspectAwareProxyFactory implements Factory<AspectAwareProxyEntity> {

	private final CompilerRepository compilerRepository;
	private final TypeExtractor typeExtractor;
	private final AspectAwareProxyService proxyService;

	public AspectAwareProxyFactory(
			CompilerRepository compilerRepository,
			TypeExtractor typeExtractor,
			AspectAwareProxyService proxyService
	) {
		this.compilerRepository = compilerRepository;
		this.typeExtractor = typeExtractor;
		this.proxyService = proxyService;
	}

	@Override
	public AspectAwareProxyEntity create(TypeElement typeElement) {
		List<ProxyMethod> eligibleMethods = proxyService.findEligibleMethods(typeElement);
		if (eligibleMethods.isEmpty()) {
			return null;
		}


		final AspectAwareProxyEntity entity = compilerRepository.newAspectAwareProxy(typeElement);
		final NameContext nameContext = new NameContext();

		eligibleMethods.forEach(proxyMethod -> entity.proxyMethod(proxyMethod, nameContext));
		entity.addWiredAnnotationFor(typeExtractor.getAllSuperTypes(typeElement))
				.addConstructorInvocation(TypeUtils.findPrimaryConstructor(typeElement).orElse(null));

		return entity;
	}

}
