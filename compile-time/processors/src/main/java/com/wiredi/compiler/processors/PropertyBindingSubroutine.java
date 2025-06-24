package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.processor.factories.IdentifiableProviderFactory;
import com.wiredi.compiler.processor.lang.ProcessingElement;
import com.wiredi.compiler.processor.lang.AnnotationProcessorSubroutine;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AutoService(AnnotationProcessorSubroutine.class)
public class PropertyBindingSubroutine implements AnnotationProcessorSubroutine {

    private static final Logger logger = LoggerFactory.getLogger(PropertyBindingSubroutine.class);

    @Inject
    private IdentifiableProviderFactory factory;

    @Override
    public List<Class<? extends Annotation>> targetAnnotations() {
        return Collections.singletonList(PropertyBinding.class);
    }

    @Override
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
        Optional<PropertyBinding> propertyBinding = Annotations.getAnnotation(typeElement, PropertyBinding.class);
        if (propertyBinding.isEmpty()) {
            logger.error("Failed to find a PropertyBinding instance!");
            return;
        }

        if (factory.create(typeElement, propertyBinding.get()) == null) {
            logger.error("The factory did not successfully create an IdentifiableProvider");
        }
    }
}
