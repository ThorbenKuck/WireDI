package com.github.thorbenkuck.di.processor.foundation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DiProcessor extends AbstractProcessor {

    private final List<Element> doneProcessing = new ArrayList<>();

    protected abstract Collection<Class<? extends Annotation>> supportedAnnotations();

    protected abstract void handle(Element element);

    protected boolean hasBeenProcessed(Element typeElement) {
        return doneProcessing.contains(typeElement);
    }

    protected void markAsProcessed(Element typeElement) {
        doneProcessing.add(typeElement);
    }

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotations().stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized final void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Logger.setMessager(processingEnv.getMessager());
        ProcessorContext.update(processingEnv);
    }

    private Set<? extends Element> findAllAnnotatedClasses(TypeElement annotation, RoundEnvironment roundEnvironment) {
        return roundEnvironment.getElementsAnnotatedWith(annotation);
    }

    private Set<? extends Element> analyzeInclusive(Set<? extends Element> foundElements, RoundEnvironment roundEnvironment) {
        Set<Element> result = new HashSet<>();
        for (Element element : foundElements) {
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
                Logger.log("Found a meta annotation!");
                Set<? extends Element> meta = findAllAnnotatedClasses((TypeElement) element, roundEnvironment);
                Collection<? extends Element> elements = analyzeInclusive(meta, roundEnvironment);
                result.addAll(elements);
            } else {
                result.add(element);
            }
        }

        return result;
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        int i = 0;
        Logger.setUseSystemOut(true);
        for (Class<? extends Annotation> type : supportedAnnotations()) {
            Set<? extends Element> root = roundEnv.getElementsAnnotatedWith(type);
            Set<? extends Element> toProcess = analyzeInclusive(root, roundEnv);

            for (Element element : toProcess) {
                Logger.setRootElement(element);
                Logger.setCurrentAnnotation(type);
                if (!hasBeenProcessed(element)) {
                    try {
                        Logger.log("[" + i++ + "] Attempting to process the annotation " + type.getName());
                        handle(element);
                        Logger.log("[" + i + "] Finished Successfully");
                        markAsProcessed(element);
                    } catch (ProcessingException e) {
                        Logger.error(e.getMessage(), e.getElement());
                    } catch (Exception e) {
                        Logger.error("[" + i + "] Encountered an unexpected Exception " + e);
                        Logger.catching(e);
                    }
                }
            }
        }

        return true;
    }
}
