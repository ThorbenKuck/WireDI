package com.wiredi.compiler.processor.lang.processors;

import com.wiredi.compiler.Injector;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.logger.messager.MessagerRegistration;
import com.wiredi.compiler.processor.ProcessorProperties;
import com.wiredi.compiler.processor.lang.AnnotationProcessorResourceResolver;
import com.wiredi.compiler.processor.lang.concurrent.ContextRunnable;
import com.wiredi.compiler.processor.lang.concurrent.ProcessorThreadFactory;
import com.wiredi.compiler.processor.lang.concurrent.ThreadBarrier;
import com.wiredi.compiler.processor.plugins.ProcessorPluginContext;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.async.DataAccess;
import jakarta.inject.Inject;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class WireBaseProcessor extends AbstractProcessor {

    private final List<Element> processed = new ArrayList<>();

    private final DataAccess dataAccess = new DataAccess();

    private final Injector injector = new Injector();

    private final Environment environment = new Environment();

    @Inject
    protected ProcessorProperties properties;

    @Inject
    protected Types types;

    @Inject
    protected Elements elements;

    @Inject
    protected Filer filer;

    private ExecutorService executorService;

    @Inject
    private Logger logger = Logger.get(getClass());

    @Inject
    private CompilerRepository repository;

    public WireBaseProcessor() {
        logger.info(() -> "Initializing " + getClass().getSimpleName());
        injector.bind(Logger.class).toConstructor((caller, type) -> Logger.get(caller));
    }

    protected abstract List<Class<? extends Annotation>> targetAnnotations();

    protected abstract void handle(Element element);

    protected final boolean hasBeenProcessed(Element typeElement) {
        return dataAccess.readValue(() -> processed.contains(typeElement));
    }

    protected final void markAsProcessed(Element typeElement) {
        dataAccess.writeValue(() -> processed.add(typeElement));
    }

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return targetAnnotations()
                .stream()
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
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            types = injector.bind(Types.class).toValue(processingEnv.getTypeUtils());
            elements = injector.bind(Elements.class).toValue(processingEnv.getElementUtils());
            filer = injector.bind(Filer.class).toValue(processingEnv.getFiler());

            environment.resourceLoader().addProtocolResolver(new AnnotationProcessorResourceResolver(filer));
            environment.autoconfigure();
            injector.bind(Environment.class).toInstance(environment);
            MessagerRegistration.announce(processingEnv.getMessager());

            injector.bind(ProcessingEnvironment.class).toValue(processingEnv);
            injector.get(ProcessorProperties.class).addOptions(processingEnv.getOptions());
            injector.postProcess(this);
            injector.get(ProcessorPluginContext.class);

            executorService = Executors.newFixedThreadPool(
                    1, // properties.getCount(PropertyKeys.PARALLEL_THREAD_COUNT, Runtime.getRuntime().availableProcessors()),
                    new ProcessorThreadFactory()
            );

            doInitialization();
            logger.info(() -> getClass().getSimpleName() + " initialized");
        } catch (Throwable throwable) {
            logger.error(() -> "Error while setting up the annotation processor " + getClass().getName());
            logger.catching(throwable);
            throw throwable;
        }
    }

    /**
     * This method can be overwritten, to be informed once the processor is initialized
     */
    protected void doInitialization() {
    }

    /**
     * This method can be overwritten, to be informed of the processing over round
     */
    protected void processingOver() {
        // NoOp, override to change behavior
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        if (roundEnv.processingOver()) {
            logger.info(() -> getClass().getSimpleName() + " finalized");
            processingOver();
        } else {
            targetAnnotations().forEach(annotation -> {
                logger.debug(() -> "Starting processing round for annotation " + annotation.getSimpleName());
                Logger.trackAnnotation(annotation, () -> {
                    dispatchProcessingRound(annotation, roundEnv);
                    logger.debug(() -> "Finished processing round for annotation " + annotation.getSimpleName());
                });
            });
        }
        logger.debug(() -> "Writing all created classes in " + repository);
        repository.flush();
        return true;
    }

    private void dispatchProcessingRound(Class<? extends Annotation> annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> root = roundEnv.getElementsAnnotatedWith(annotation);
        if (root.isEmpty()) {
            logger.debug(() -> "No annotated elements found in current RoundEnvironment");
            return;
        }
        Set<? extends Element> toProcess = analyzeInclusive(annotation, root, roundEnv);
        if (toProcess.isEmpty()) {
            logger.debug(() -> "No elements to process found in current RoundEnvironment");
            return;
        }

        logger.debug(() -> "Found " + toProcess.size() + " elements to process. Dispatching async processing");
        ThreadBarrier.Builder barrierBuilder = ThreadBarrier.builder()
                .withExecutorService(executorService);

        toProcess.forEach(element -> {
            if (element != null && !hasBeenProcessed(element)) {
                logger.debug(() -> "Dispatching processing round for " + element);
                barrierBuilder.withRunnable(new ProcessRunnable(element, annotation));
            } else {
                logger.debug(() -> "Found null element");
            }
        });

        logger.debug(() -> "Awaiting finish of dispatch processes");
        barrierBuilder.run();
        logger.debug(() -> "All dispatch processes have finished");
    }

    private Set<? extends Element> findAllAnnotatedClasses(TypeElement annotation, RoundEnvironment roundEnvironment) {
        return roundEnvironment.getElementsAnnotatedWith(annotation);
    }

    private Set<? extends Element> analyzeInclusive(Class<? extends Annotation> annotation, Set<? extends Element> foundElements, RoundEnvironment roundEnvironment) {
        Set<Element> result = new HashSet<>();
        for (Element element : foundElements) {
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
                logger.info(() -> "Found a meta annotation. Annotation " + element + " is annotated with " + annotation);
                Set<? extends Element> meta = findAllAnnotatedClasses((TypeElement) element, roundEnvironment);
                Collection<? extends Element> elements = analyzeInclusive(annotation, meta, roundEnvironment);
                result.addAll(elements);
            } else {
                result.add(element);
            }
        }

        return result;
    }

    private final class ProcessRunnable implements ContextRunnable {

        private final Element rootElement;
        private final Class<? extends Annotation> annotationType;

        private ProcessRunnable(
                Element rootElement,
                Class<? extends Annotation> annotationType
        ) {
            this.rootElement = rootElement;
            this.annotationType = annotationType;
        }

        @Override
        public void run() {
            if (hasBeenProcessed(rootElement)) {
                return;
            }
            process();
        }

        private void process() {
            Logger.trackAnnotation(annotationType, () -> {
                Logger.trackRootElement(rootElement, () -> {
                    try {
                        handle(rootElement);
                        markAsProcessed(rootElement);
                    } catch (ProcessingException e) {
                        logger.error(e.getElement(), e.getMessage());
                        logger.catching(e);
                    } catch (Throwable e) {
                        logger.error(rootElement, "Unexpected error while processing " + rootElement.getSimpleName() + ": " + e.getMessage());
                        logger.catching(e);
                    }
                });
            });
        }
    }
}
