package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.annotations.stereotypes.Configuration;
import com.wiredi.annotations.stereotypes.DefaultConfiguration;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.processor.lang.AnnotationProcessorSubroutine;
import com.wiredi.compiler.processor.lang.ProcessingElement;
import com.wiredi.compiler.processors.adapter.IdentifiableProviderWireAdapter;
import com.wiredi.compiler.processors.adapter.InterfaceImplementationAdapter;
import com.wiredi.runtime.domain.annotations.AnnotationExcerpt;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.List;

@AutoService(AnnotationProcessorSubroutine.class)
public class WireProcessorSubroutine implements AnnotationProcessorSubroutine {

    private static final Logger logger = LoggerFactory.getLogger(WireProcessorSubroutine.class);
    @Inject
    private IdentifiableProviderWireAdapter wireAdapter;
    @Inject
    private InterfaceImplementationAdapter interfaceImplementationAdapter;
    @Inject
    private Annotations annotations;

    @Override
    public List<Class<? extends Annotation>> targetAnnotations() {
        return List.of(Wire.class, Configuration.class, AutoConfiguration.class, DefaultConfiguration.class);
    }

    @Override
    public void handle(ProcessingElement processingElement) {
        Element element = processingElement.element();
        if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
            return;
        }
        AnnotationExcerpt<Wire> wireAnnotationExcerpt = Annotations.search().byType(Wire.class)
                .findFirstExcerptIn(element)
                .orElse(null);

        logger.debug("Handling {}", element);
        if (!(element instanceof TypeElement typeElement)) {
            logger.error("The annotated element of Wire has to be a class!");
            return;
        }

        if (element.getKind().isInterface()) {
            interfaceImplementationAdapter.handle(typeElement, wireAnnotationExcerpt);
        } else {
            wireAdapter.handle(typeElement, wireAnnotationExcerpt);
        }
    }
}
