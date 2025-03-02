package com.wiredi.compiler.processors;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.processor.lang.processors.WireBaseProcessor;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

import static com.wiredi.compiler.processor.CompilerPropertyKeys.ENABLE_GENERATED_SOURCES;

@AutoService(Processor.class)
public class GeneratedClassesProcessor extends WireBaseProcessor {
    private final Set<String> generatedClasses = new HashSet<>();
    private final Logger logger = Logger.get(GeneratedClassesProcessor.class);
    private boolean enabled;

    @Override
    protected List<Class<? extends Annotation>> targetAnnotations() {
        return List.of(jakarta.annotation.Generated.class, javax.annotation.processing.Generated.class);
    }

    @Override
    protected void doInitialization() {
        this.enabled = properties.isEnabled(ENABLE_GENERATED_SOURCES);
    }

    @Override
    protected void handle(Element element) {
        if (this.enabled) {
            generatedClasses.add(((TypeElement) element).getQualifiedName().toString());
        }
    }

    @Override
    protected void processingOver() {
        if (!generatedClasses.isEmpty()) {
            writeGeneratedClassesFile();
        }
    }

    private void writeGeneratedClassesFile() {
        logger.info("Writing generated classes file with content: " + generatedClasses);
        try {
            Filer filer = processingEnv.getFiler();
            FileObject file = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "META-INF/generated_classes.txt");
            try (Writer writer = file.openWriter()) {
                for (String className : generatedClasses) {
                    writer.write(className + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(() -> "Error while writing generated_classes.txt!");
            logger.error(() -> e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
