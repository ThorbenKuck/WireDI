package com.github.thorbenkuck.di.processor.extensions;

import com.github.thorbenkuck.di.processor.WireInformation;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface WireExtension {

    List<Class<? extends Annotation>> supportedAnnotations();

    @NotNull
    default List<WireInformation> extractWireInformation(Set<? extends Element> elements) { return Collections.emptyList(); }

}
