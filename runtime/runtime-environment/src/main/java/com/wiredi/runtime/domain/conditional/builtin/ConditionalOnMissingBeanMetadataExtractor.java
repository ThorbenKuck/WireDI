package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.annotations.AnnotationMetadataExtractor;
import com.wiredi.runtime.domain.annotations.ExtractionContext;
import com.wiredi.runtime.domain.annotations.ExtractionEnvironment;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class ConditionalOnMissingBeanMetadataExtractor implements AnnotationMetadataExtractor {

    private final Logging logging = Logging.getInstance(ConditionalOnMissingBeanMetadataExtractor.class);

    @Override
    public @Nullable AnnotationMetadata extract(ExtractionContext context, ExtractionEnvironment environment) {
        if (!context.annotationMetadata().isOfType(ConditionalOnMissingBean.class)) {
            return null;
        }

        logging.info(() -> "Extracting from " + context.annotatedElement().getSimpleName().toString() + " with metadata " + context.annotationMetadata());
        if (context.annotationMetadata().getRawClass("value").orElse(Void.class).equals(Void.class)
                && context.annotationMetadata().getRawClass("type").orElse(Void.class).equals(Void.class)) {
            AnnotationMetadata.Builder builder = AnnotationMetadata.builder(ConditionalOnMissingBean.class);

            if (context.annotatedElement() instanceof TypeElement t) {
                return builder.withField("type", t)
                        .withField("value", t)
                        .build();
            }

            if (context.annotatedElement() instanceof ExecutableElement e) {
                return builder.withField("type", e.getReturnType())
                        .withField("value", e.getReturnType())
                        .build();
            }

            throw new IllegalStateException("Unsupported annotated element type: " + context.annotatedElement().getClass().getName());
        }

        return context.annotationMetadata();
    }
}
