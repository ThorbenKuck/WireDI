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
    private static final String VOID_TYPE = "java.lang.Void";

    @Override
    public @Nullable AnnotationMetadata extract(ExtractionContext context, ExtractionEnvironment environment) {
        AnnotationMetadata annotationMetadata = context.annotationMetadata();
        if (!annotationMetadata.isOfType(ConditionalOnMissingBean.class)) {
            return null;
        }

        // Instead of trying to get Class<?>, work with type names
        String valueTypeName = annotationMetadata.getTypeName("value", VOID_TYPE);
        String typeTypeName = annotationMetadata.getTypeName("type", VOID_TYPE);
        
        logging.info(() -> "Extracting ConditionalOnMissingBean annotation from " + 
                      context.annotatedElement().getSimpleName().toString() + 
                      " with metadata " + annotationMetadata + 
                      ". ValueType=" + valueTypeName + ", Type=" + typeTypeName);
        
        if (valueTypeName.equals(VOID_TYPE) && typeTypeName.equals(VOID_TYPE)) {
            AnnotationMetadata.Builder builder = AnnotationMetadata.builder(ConditionalOnMissingBean.class);

            if (context.annotatedElement() instanceof TypeElement t) {
                logging.info(() -> "@ConditionalOnMissingBean on class with type " + t.getSimpleName().toString());
                return builder.withField("type", t)
                        .withField("value", t)
                        .build();
            }

            if (context.annotatedElement() instanceof ExecutableElement e) {
                logging.info(() -> "@ConditionalOnMissingBean on method with type " + e.getReturnType().toString());
                return builder.withField("type", e.getReturnType())
                        .withField("value", e.getReturnType())
                        .build();
            }

            throw new IllegalStateException("Unsupported annotated element type: " + context.annotatedElement().getClass().getName());
        }

        return annotationMetadata;
    }
}