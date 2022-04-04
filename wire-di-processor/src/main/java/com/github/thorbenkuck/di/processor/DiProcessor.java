package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.processor.concurrent.ThreadBarrier;
import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DiProcessor extends AbstractProcessor {

	private final List<Element> doneProcessing = new ArrayList<>();
	private static final ExecutorService executorService = Executors.newFixedThreadPool(
			ProcessorProperties.getCount(PropertyKeys.PARALLEL_THREAD_COUNT),
			new ProcessorThreadFactory()
	);
	protected Types types;
	protected Elements elements;
	protected Filer filer;

	protected abstract Class<? extends Annotation> targetAnnotation();

	protected abstract void handle(Element element);

	protected void finish() {
	}

	protected final boolean hasBeenProcessed(Element typeElement) {
		synchronized(doneProcessing) { return doneProcessing.contains(typeElement); }
	}

	protected final void markAsProcessed(Element typeElement) {
		synchronized(doneProcessing) { doneProcessing.add(typeElement);}
	}

	@Override
	public final Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(targetAnnotation().getCanonicalName());
	}

	@Override
	public final SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public synchronized final void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();

		Logger.setMessager(processingEnv.getMessager());
		ProcessorContext.setThreadState(processingEnv);
		Logger.info("%s initialized", getClass().getSimpleName());
	}

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			finish();
			Logger.info("%s finalized", getClass().getSimpleName());
		} else {
			Logger.debug("Starting processing round for annotation %s", targetAnnotation().getSimpleName());
			dispatchProcessingAround(roundEnv);
			Logger.debug("Finished processing round for annotation %s", targetAnnotation().getSimpleName());
		}

		writeClasses();
		return true;
	}

	private Set<? extends Element> findAllAnnotatedClasses(TypeElement annotation, RoundEnvironment roundEnvironment) {
		return roundEnvironment.getElementsAnnotatedWith(annotation);
	}

	private Set<? extends Element> analyzeInclusive(Set<? extends Element> foundElements, RoundEnvironment roundEnvironment) {
		Set<Element> result = new HashSet<>();
		for (Element element : foundElements) {
			if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
				Logger.debug("Found a meta annotation!");
				Set<? extends Element> meta = findAllAnnotatedClasses((TypeElement) element, roundEnvironment);
				Collection<? extends Element> elements = analyzeInclusive(meta, roundEnvironment);
				result.addAll(elements);
			} else {
				result.add(element);
			}
		}

		return result;
	}

	private void dispatchProcessingAround(RoundEnvironment roundEnv) {
		Set<? extends Element> root = roundEnv.getElementsAnnotatedWith(targetAnnotation());
		if (root.isEmpty()) {
			Logger.debug("No annotated elements found in current RoundEnvironment");
			return;
		}
		Set<? extends Element> toProcess = analyzeInclusive(root, roundEnv);
		if (toProcess.isEmpty()) {
			Logger.debug("No elements to process found in current RoundEnvironment");
			return;
		}

		Logger.debug("Found %s elements to process. Dispatching async processing", toProcess.toString());
		ThreadBarrier.Builder barrierBuilder = ThreadBarrier.builder()
				.withExecutorService(executorService);

		toProcess.forEach(element -> {
			if (element != null) {
				Logger.debug("Dispatching processing round for %s", element.toString());
				barrierBuilder.withRunnable(new ProcessRunnable(element, targetAnnotation()));
			} else {
				Logger.debug("Found null element");
			}
		});

		Logger.debug("Awaiting finish of dispatch processes");
		barrierBuilder.run();
		Logger.debug("All dispatch processes have finished");
	}

	private synchronized void writeClasses() {
		Queue<ClassWriter.WritingInformation> queue = ClassWriter.drain();
		if(queue.size() > 0) {
			Logger.debug("Writing %s classes", Integer.toString(queue.size()));
		}

		while(queue.peek() != null) {
			ClassWriter.WritingInformation information = queue.poll();

			TypeSpec typeSpec = information.getTypeSpec();
			Element rootElement = information.getRootElement();
			PackageElement packageElement = Objects.requireNonNull(elements, "Elements not set")
					.getPackageOf(rootElement);

			Logger.debug("Writing class " + information.getTypeSpec().name);
			try {
				JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec)
						.indent("    ")
						.build()
						.writeTo(Objects.requireNonNull(filer, "Filer not set"));
			} catch (IOException e) {
				throw new ProcessingException(rootElement, e.getMessage());
			}
		}
	}

	private final class ProcessRunnable implements Runnable {

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

			try {
				ProcessorContext.setThreadState(processingEnv);
				Logger.setThreadState(annotationType, rootElement);

				process();
			} finally {
				Logger.clearThreadState();
				ProcessorContext.clearThreadState();
				ProcessorContext.clearThreadState();
			}
		}

		private void process() {
			try {
				handle(rootElement);
				markAsProcessed(rootElement);
			} catch (ProcessingException e) {
				Logger.catching(e);
			} catch (Exception e) {
				Logger.catching(e);
			}
		}
	}
}
