package com.wiredi.compiler.processor.lang;

import com.google.auto.service.AutoService;
import com.wiredi.compiler.CompilerEnvironment;
import com.wiredi.compiler.Injector;
import com.wiredi.compiler.ThreadSafeElements;
import com.wiredi.compiler.ThreadSafeTypes;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.errors.CompositeProcessingException;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.CompilerServiceFileProviderFactory;
import com.wiredi.compiler.logger.slf4j.MessagerContext;
import com.wiredi.compiler.logger.slf4j.Slf4jHijacker;
import com.wiredi.compiler.processor.ProcessorProperties;
import com.wiredi.compiler.processor.lang.concurrent.ContextRunnable;
import com.wiredi.compiler.processor.lang.concurrent.ExecutorServiceThreadBarrier;
import com.wiredi.compiler.processor.lang.concurrent.ProcessorThreadFactory;
import com.wiredi.compiler.processor.plugins.ProcessorPluginContext;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.ServiceFiles;
import com.wiredi.runtime.lang.Counter;
import com.wiredi.runtime.properties.Key;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.*;
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

@AutoService(Processor.class)
public class WireDiRootAnnotationProcessor extends AbstractProcessor {

    private static final Slf4jHijacker slf4jHijacker = new Slf4jHijacker();

    static {
        ServiceFiles.setProviderFactory(new CompilerServiceFileProviderFactory());
        slf4jHijacker.hijackSlf4j();
    }

    private final Injector injector = new Injector();
    private final List<AnnotationProcessorSubroutine> subroutines = new ArrayList<>();
    private final List<Class<? extends Annotation>> supportedAnnotations = new ArrayList<>();
    private final Set<Element> processedElements = new HashSet<>();
    private final Logger logger = LoggerFactory.getLogger(WireDiRootAnnotationProcessor.class);
    private final Counter counter = new Counter();
    private final Environment environment = CompilerEnvironment.get();
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
    private CompilerRepository repository;

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return subroutines
                .stream()
                .flatMap(it -> it.targetAnnotations().stream())
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
            processingEnv.getOptions().forEach((key, value) -> environment.setProperty(Key.format(key), value));
            slf4jHijacker.initialize(environment, processingEnv.getMessager());

            counter.reset();
            logger.debug("Hijacked SLF4J with messager: {}", processingEnv.getMessager());

            injector.bind(Environment.class).toInstance(environment);
            injector.bind(ProcessingEnvironment.class).toValue(processingEnv);
            types = injector.bind(Types.class).toValue(new ThreadSafeTypes(processingEnv.getTypeUtils()));
            elements = injector.bind(Elements.class).toValue(new ThreadSafeElements(processingEnv.getElementUtils()));
            filer = injector.bind(Filer.class).toValue(processingEnv.getFiler());
            injector.bind(Locale.class).toValue(processingEnv.getLocale());
            injector.bind(SourceVersion.class).toValue(processingEnv.getSourceVersion());

            injector.get(ProcessorProperties.class).addOptions(processingEnv.getOptions());
            injector.postProcess(this);
            injector.get(ProcessorPluginContext.class);

            executorService = Executors.newFixedThreadPool(
                    1, // properties.getCount(PropertyKeys.PARALLEL_THREAD_COUNT, Runtime.getRuntime().availableProcessors()),
                    new ProcessorThreadFactory()
            );

//            executorService = Executors.newVirtualThreadPerTaskExecutor();

            List<? extends Class<? extends AnnotationProcessorSubroutine>> processorTypes = ServiceLoader.load(AnnotationProcessorSubroutine.class)
                    .stream()
                    .map(ServiceLoader.Provider::type)
                    .toList();

            this.subroutines.addAll(processorTypes.stream().map(injector::get).toList());
            this.supportedAnnotations.addAll(
                    this.subroutines.stream()
                            .flatMap(it -> it.targetAnnotations().stream())
                            .toList()
            );

            logger.info("Loaded Subroutines({}): {}", processorTypes.size(), processorTypes);
            logger.info("Supported Annotations({}): {}", supportedAnnotations.size(), this.supportedAnnotations);

            doInitialization();
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("Error while setting up the instance processor " + getClass().getName() + ": " + throwable.getMessage(), throwable);
            }
            throw throwable;
        }
    }

    private void doInitialization() {
        this.subroutines.forEach(AnnotationProcessorSubroutine::doInitialization);
    }

    private void processingOver() {
        this.subroutines.forEach(AnnotationProcessorSubroutine::processingOver);
        injector.tearDown();
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        counter.increment();
        logger.debug("Starting processing round {}", counter.get());
        logger.debug("Round contents: {}", roundEnv.getRootElements());
        if (roundEnv.processingOver()) {
            logger.debug("Processing over. Informing subprocessors.");
            processingOver();
        } else {
            if (subroutines.isEmpty()) {
                logger.warn("No sub processors found. Skipping processing");
                return true;
            }
            Set<Element> touchedElements = new HashSet<>();
            ExecutorServiceThreadBarrier.Builder barrierBuilder = ExecutorServiceThreadBarrier.builder()
                    .withExecutorService(executorService);
            Annotations annotationsUtils = injector.get(Annotations.class);

            this.subroutines.forEach(subProcessor -> {
                subProcessor.targetAnnotations().forEach(annotation -> {
                    Set<? extends Element> elements = findElementsWithAnnotation(annotation, roundEnv);

                    if (!elements.isEmpty()) {
                        logger.debug("Found Elements({}) to be processed by {}", elements.size(), subProcessor.getClass().getSimpleName());
                        dispatchProcessingRound(subProcessor, annotation, elements, annotationsUtils, barrierBuilder);
                        touchedElements.addAll(elements);
                    }
                });
            });

            logger.debug("Awaiting finish of dispatch processes");
            barrierBuilder.run();
            logger.debug("All dispatch processes have finished");
            processedElements.addAll(touchedElements);
        }

        if (repository.hasEntities()) {
            logger.debug("Writing all created classes in {}", repository);
            repository.flush(filer);
        }
        logger.debug("Finished processing round {}", counter.get());
        return true;
    }

    private Set<? extends Element> findElementsWithAnnotation(Class<? extends Annotation> annotation, RoundEnvironment roundEnvironment) {
        Set<Element> elements = new HashSet<>();
        for (Element element : roundEnvironment.getRootElements()) {
            if (!processedElements.contains(element) && Annotations.isAnnotatedWith(element, annotation)) {
                elements.add(element);
            }
        }

        return elements;
    }

    private void dispatchProcessingRound(
            AnnotationProcessorSubroutine subProcessor,
            Class<? extends Annotation> annotation,
            Set<? extends Element> elements,
            Annotations annotations,
            ExecutorServiceThreadBarrier.Builder barrierBuilder
    ) {
        logger.debug("Found {} elements to process. Dispatching async processing", elements.size());
        elements.forEach(element -> {
            if (element != null) {
                logger.debug("Dispatching processing round for {}", element);
                barrierBuilder.withRunnable(new ProcessRunnable(subProcessor, annotation, element, annotations));
            } else {
                logger.debug("Found null element");
            }
        });
    }

    private Set<? extends Element> findAllAnnotatedClasses(TypeElement annotation, RoundEnvironment roundEnvironment) {
        return roundEnvironment.getElementsAnnotatedWith(annotation);
    }

    private Set<? extends Element> analyzeInclusive(Set<Class<? extends Annotation>> annotations, Set<? extends Element> foundElements, RoundEnvironment roundEnvironment) {
        Set<Element> result = new HashSet<>();
        for (Element element : foundElements) {
            if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
                logger.info("Found a meta annotations. Annotation {} is annotated with {}", element, annotations);
                Set<? extends Element> meta = findAllAnnotatedClasses((TypeElement) element, roundEnvironment);
                Collection<? extends Element> elements = analyzeInclusive(annotations, meta, roundEnvironment);
                result.addAll(elements);
            } else {
                result.add(element);
            }
        }

        return result;
    }

    private final class ProcessRunnable implements ContextRunnable {

        private final AnnotationProcessorSubroutine processor;
        private final Class<? extends Annotation> matchedAnnotationType;
        private final Element rootElement;
        private final Annotations annotations;

        private ProcessRunnable(
                AnnotationProcessorSubroutine processor,
                Class<? extends Annotation> matchedAnnotationType,
                Element rootElement,
                Annotations annotations
        ) {
            this.processor = processor;
            this.matchedAnnotationType = matchedAnnotationType;
            this.rootElement = rootElement;
            this.annotations = annotations;
        }

        @Override
        public void run() {
            logger.debug("Processing {}({})", rootElement.getKind(), rootElement.getSimpleName());
            ProcessingElement processingElement = new ProcessingElement(rootElement, annotations, matchedAnnotationType);
            MessagerContext.runIsolated(instance -> {
                instance.setAnnotationMirror(processingElement.annotationMirror())
                        .setElement(rootElement)
                        .setAnnotationType(matchedAnnotationType);

                try {
                    processor.handle(processingElement);
                } catch (CompositeProcessingException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Multiple errors while processing " + rootElement.getSimpleName() + ": " + e.getMessage(), e);
                    }
                    e.getExceptions().forEach(processingException -> {
                        MessagerContext.runNested(i -> {
                            i.setElement(processingException.getElement());
                            logger.error(processingException.getMessage());
                        });
                    });
                } catch (ProcessingException e) {
                    MessagerContext.runNested(i -> {
                        i.setElement(e.getElement());
                        logger.error(e.getMessage());
                    });
                } catch (Throwable e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Unexpected error while processing " + rootElement.getSimpleName() + ": " + e.getClass() + ": " + e.getMessage(), e);
                    }
                }
            });
        }
    }
}
