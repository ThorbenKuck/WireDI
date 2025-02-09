package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.properties.PropertySource;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.EnvironmentConfigurationEntity;
import com.wiredi.compiler.domain.entities.environment.EnvironmentModification;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.compiler.processor.factories.EnvironmentConfigurationFactory;
import com.wiredi.compiler.processor.lang.processors.WireBaseProcessor;
import com.wiredi.runtime.properties.Key;
import jakarta.inject.Inject;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoService(Processor.class)
public class PropertySourceWireProcessor extends WireBaseProcessor {

    @Inject
    private Logger logger;

    @Inject
    private EnvironmentConfigurationFactory factory;

    @Inject
    private CompilerRepository compilerRepository;

    @Override
    protected List<Class<? extends Annotation>> targetAnnotations() {
        return Collections.singletonList(PropertySource.class);
    }

    @Override
    protected void handle(Element element) {
        if (!element.getKind().isClass()) {
            logger.error(element, "The annotated element of PropertySource has to be a class!");
            return;
        }
        final TypeElement typeElement = (TypeElement) element;
        Optional<PropertySource> propertySourceOptional = Annotations.getAnnotation(typeElement, PropertySource.class);
        if (propertySourceOptional.isEmpty()) {
            logger.error(element, "Failed to find a PropertySource annotation!");
            return;
        }
        PropertySource propertySource = propertySourceOptional.get();

        compilerRepository.newEnvironmentConfiguration(typeElement)
                .appendSourceFiles(propertySource.value())
                .appendModifications(
                        Arrays.stream(propertySource.entries())
                                .map(it -> EnvironmentModification.addProperty(Key.format(it.key()), it.value()))
                                .toList()
                );
    }
}
