package com.wiredi.compiler.processor.factories;

import com.wiredi.annotations.properties.PropertySource;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.EnvironmentConfigurationEntity;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.properties.Key;
import jakarta.inject.Inject;

import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Optional;

public class EnvironmentConfigurationFactory implements Factory<EnvironmentConfigurationEntity> {

	@Inject
	private CompilerRepository compilerRepository;

	private static final Logger logger = Logger.get(EnvironmentConfigurationFactory.class);

	@Override
	public EnvironmentConfigurationEntity create(TypeElement typeElement) {
		Optional<PropertySource> annotation = Annotations.getAnnotation(typeElement, PropertySource.class);
		if (annotation.isEmpty()) {
			throw new ProcessingException(typeElement, "Missing @PropertySource annotation.");
		}
		PropertySource propertySource = annotation.get();
		return compilerRepository.newEnvironmentConfiguration(typeElement)
				.appendSourceFiles(propertySource.value())
				.appendEntries(
						Arrays.stream(propertySource.entries())
								.map(it -> new EnvironmentConfigurationEntity.Entry(Key.format(it.key()), it.value()))
								.toList()
				);
	}
}
