package com.github.thorbenkuck.di.processor.extensions;

import com.github.thorbenkuck.di.processor.WireInformation;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public interface WireExtension {

    @NotNull
    List<@NotNull Class<? extends Annotation>> supportedAnnotations();

    @NotNull
    default List<WireInformation> extractWireInformation(@NotNull Element element) { return Collections.emptyList(); }

    default void preExtraction(@NotNull Element element) {
    }

    default void postExtraction(@NotNull Element element) {
    }

    default void postClassCreation(@NotNull Element element, List<WireInformation> wireInformation) {
    }

    default boolean willProcess(@NotNull Element element) { return true; }

}
