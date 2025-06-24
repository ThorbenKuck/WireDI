package com.wiredi.compiler.processor.factories;

import com.wiredi.annotations.properties.PropertySource;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.EnvironmentConfigurationEntity;
import com.wiredi.compiler.domain.entities.environment.AddPropertyEnvironmentModification;
import com.wiredi.compiler.domain.entities.environment.EnvironmentModification;
import com.wiredi.compiler.errors.ProcessingException;
import org.slf4j.Logger;import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.properties.Key;
import jakarta.inject.Inject;

import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Optional;

public class EnvironmentConfigurationFactory implements Factory<EnvironmentConfigurationEntity> {

	@Inject
	private CompilerRepository compilerRepository;

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(EnvironmentConfigurationFactory.class);

	@Override
	public EnvironmentConfigurationEntity create(TypeElement typeElement) {
		Optional<PropertySource> annotation = Annotations.getAnnotation(typeElement, PropertySource.class);
		if (annotation.isEmpty()) {
			throw new ProcessingException(typeElement, "Missing @PropertySource instance.");
		}
		PropertySource propertySource = annotation.get();
		return compilerRepository.newEnvironmentConfiguration(typeElement)
				.appendSourceFiles(propertySource.value())
				.appendModifications(
						Arrays.stream(propertySource.entries())
								.map(it -> EnvironmentModification.addProperty(Key.format(it.key()), it.value()))
								.toList()
				);
	}
}
