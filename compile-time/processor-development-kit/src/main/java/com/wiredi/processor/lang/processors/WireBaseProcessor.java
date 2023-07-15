package com.wiredi.processor.lang.processors;

import com.wiredi.Injector;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.logger.messager.MessagerRegistration;
import com.wiredi.compiler.repository.CompilerRepository;
import com.wiredi.environment.Environment;
import com.wiredi.lang.DataAccess;
import com.wiredi.processor.ProcessorProperties;
import com.wiredi.processor.PropertyKeys;
import com.wiredi.processor.lang.concurrent.ContextRunnable;
import com.wiredi.processor.lang.concurrent.ProcessorThreadFactory;
import com.wiredi.processor.lang.concurrent.ThreadBarrier;
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

import static com.wiredi.ConstructionResult.doNotCache;

public abstract class WireBaseProcessor extends AbstractProcessor {

	private final List<Element> processed = new ArrayList<>();

	private final DataAccess dataAccess = new DataAccess();

	private final Injector injector = new Injector();

	private ExecutorService executorService;

	@Inject
	protected ProcessorProperties properties;

	@Inject
	protected Types types;

	@Inject
	protected Elements elements;

	@Inject
	protected Filer filer;

	@Inject
	private Logger logger;

	@Inject
	private CompilerRepository repository;

	private final Environment environment = new Environment();

	public WireBaseProcessor() {
		injector.bind(Logger.class).toConstructor((caller, type) -> doNotCache(Logger.get(caller)));
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

		environment.autoconfigure();
		injector.bind(Environment.class).to(environment);
		MessagerRegistration.announce(processingEnv.getMessager());
		types = injector.bind(Types.class).toValue(processingEnv.getTypeUtils());
		elements = injector.bind(Elements.class).toValue(processingEnv.getElementUtils());
		filer = injector.bind(Filer.class).toValue(processingEnv.getFiler());

		injector.bind(ProcessingEnvironment.class).toValue(processingEnv);
		injector.get(ProcessorProperties.class).addOptions(processingEnv.getOptions());
		injector.injectInto(this);

		executorService = Executors.newFixedThreadPool(
				1, // properties.getCount(PropertyKeys.PARALLEL_THREAD_COUNT, Runtime.getRuntime().availableProcessors()),
				new ProcessorThreadFactory()
		);

		logger.info(() -> getClass().getSimpleName() + " initialized");
	}

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		if (roundEnv.processingOver()) {
			logger.info(() -> getClass().getSimpleName() + " finalized");
		} else {
			targetAnnotations().forEach(annotation -> {
				logger.debug(() -> "Starting processing round for annotation " + annotation.getSimpleName());
				Logger.trackAnnotation(annotation, () -> {
					dispatchProcessingRound(roundEnv, annotation);
					logger.debug(() -> "Finished processing round for annotation " + annotation.getSimpleName());
				});
			});
		}
		return true;
	}

	private Set<? extends Element> findAllAnnotatedClasses(TypeElement annotation, RoundEnvironment roundEnvironment) {
		return roundEnvironment.getElementsAnnotatedWith(annotation);
	}

	private Set<? extends Element> analyzeInclusive(Set<? extends Element> foundElements, RoundEnvironment roundEnvironment) {
		Set<Element> result = new HashSet<>();
		for (Element element : foundElements) {
			if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
				logger.info(() -> "Found a meta annotation");
				Set<? extends Element> meta = findAllAnnotatedClasses((TypeElement) element, roundEnvironment);
				Collection<? extends Element> elements = analyzeInclusive(meta, roundEnvironment);
				result.addAll(elements);
			} else {
				result.add(element);
			}
		}

		return result;
	}

	private void dispatchProcessingRound(RoundEnvironment roundEnv, Class<? extends Annotation> annotationType) {
		Set<? extends Element> root = roundEnv.getElementsAnnotatedWith(annotationType);
		if (root.isEmpty()) {
			logger.debug(() -> "No annotated elements found in current RoundEnvironment");
			return;
		}
		Set<? extends Element> toProcess = analyzeInclusive(root, roundEnv);
		if (toProcess.isEmpty()) {
			logger.debug(() -> "No elements to process found in current RoundEnvironment");
			return;
		}

		logger.debug(() -> "Found " + toProcess + " elements to process. Dispatching async processing");
		ThreadBarrier.Builder barrierBuilder = ThreadBarrier.builder()
				.withExecutorService(executorService);

		toProcess.forEach(element -> {
			if (element != null && !hasBeenProcessed(element)) {
				logger.debug(() -> "Dispatching processing round for " + element);
				barrierBuilder.withRunnable(new ProcessRunnable(element, annotationType));
			} else {
				logger.debug("Found null element");
			}
		});

		logger.debug(() -> "Awaiting finish of dispatch processes");
		barrierBuilder.run();
		logger.debug(() -> "Writing all created classes");
		repository.flush();
		logger.debug(() -> "All dispatch processes have finished");
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
					} catch (Exception e) {
						logger.catching(e);
					}
				});
			});
		}
	}
}
