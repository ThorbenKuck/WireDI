package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.properties.PropertySource;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.entities.environment.EnvironmentModification;
import com.wiredi.compiler.processor.lang.ProcessingElement;
import com.wiredi.compiler.processor.lang.AnnotationProcessorSubroutine;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.properties.Key;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoService(AnnotationProcessorSubroutine.class)
public class PropertySourceSubroutine implements AnnotationProcessorSubroutine {

    private static final Logger logger = LoggerFactory.getLogger(PropertySourceSubroutine.class);
    @Inject
    private CompilerRepository compilerRepository;

    public List<Class<? extends Annotation>> targetAnnotations() {
        return Collections.singletonList(PropertySource.class);
    }

    public void handle(ProcessingElement processingElement) {
        Element element = processingElement.element();
        if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
            return;
        }
        if (!element.getKind().isClass()) {
            logger.error("The annotated element of PropertySource has to be a class!");
            return;
        }
        final TypeElement typeElement = (TypeElement) element;
        Optional<PropertySource> propertySourceOptional = Annotations.getAnnotation(typeElement, PropertySource.class);
        if (propertySourceOptional.isEmpty()) {
            logger.error("Failed to find a PropertySource instance!");
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
