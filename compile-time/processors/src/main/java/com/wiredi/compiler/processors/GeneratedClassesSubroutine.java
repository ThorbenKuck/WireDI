package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.compiler.processor.ProcessorProperties;
import com.wiredi.compiler.processor.lang.ProcessingElement;
import com.wiredi.compiler.processor.lang.AnnotationProcessorSubroutine;
import jakarta.inject.Inject;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.wiredi.compiler.processor.CompilerPropertyKeys.ENABLE_GENERATED_SOURCES;

@AutoService(AnnotationProcessorSubroutine.class)
public class GeneratedClassesSubroutine implements AnnotationProcessorSubroutine {

    @Inject
    private ProcessorProperties properties;
    @Inject
    private ProcessingEnvironment processingEnvironment;
    private final Set<String> generatedClasses = new HashSet<>();
    private final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(GeneratedClassesSubroutine.class);
    private boolean enabled;

    @Override
    public List<Class<? extends Annotation>> targetAnnotations() {
        return List.of(jakarta.annotation.Generated.class, javax.annotation.processing.Generated.class);
    }

    @Override
    public void doInitialization() {
        this.enabled = properties.isEnabled(ENABLE_GENERATED_SOURCES);
    }

    @Override
    public void handle(ProcessingElement processingElement) {
        Element element = processingElement.element();
        if (this.enabled) {
            generatedClasses.add(((TypeElement) element).getQualifiedName().toString());
        }
    }

    @Override
    public void processingOver() {
        if (!generatedClasses.isEmpty()) {
            writeGeneratedClassesFile();
        }
    }

    private void writeGeneratedClassesFile() {
        logger.info("Writing generated classes file with content: " + generatedClasses);
        try {
            Filer filer = processingEnvironment.getFiler();
            FileObject file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "META-INF/generated_classes.txt");
            try (Writer writer = file.openWriter()) {
                for (String className : generatedClasses) {
                    writer.write(className + "\n");
                }
            }
        } catch (IOException e) {
            logger.error(() -> "Error while writing generated_classes.txt!", e);
        }
    }
}
