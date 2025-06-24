package com.wiredi.runtime.domain.annotations;

import org.jetbrains.annotations.Nullable;

public interface AnnotationMetadataExtractor {

    @Nullable
    AnnotationMetadata extract(ExtractionContext context, ExtractionEnvironment environment);

}
