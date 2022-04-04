package com.github.thorbenkuck.di.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Objects;
import java.util.function.Function;

public class ProcessorContext {

	private static final ThreadLocal<Types> types = new ThreadLocal<>();
	private static final ThreadLocal<Elements> elements = new ThreadLocal<>();
	private static final ThreadLocal<Filer> filer = new ThreadLocal<>();

	public static void setThreadState(ProcessingEnvironment processingEnvironment) {
		types.set(processingEnvironment.getTypeUtils());
		elements.set(processingEnvironment.getElementUtils());
		filer.set(processingEnvironment.getFiler());
	}

	public static Types getTypes() {
		return Objects.requireNonNull(types.get(), "No Types set for the current ProcessorContext");
	}

	public static <T> T mapWithElements(Function<Elements, T> elementsConsumer) {
		synchronized (ProcessorContext.class) {
			Elements elementsInstance = Objects.requireNonNull(elements.get(), "No Elements set for the current ProcessorContext");
			return elementsConsumer.apply(elementsInstance);
		}
	}

	public static Filer getFiler() {
		return Objects.requireNonNull(filer.get(), "No Filer set for the current ProcessorContext");
	}
	public static void clearThreadState() {
		types.remove();
		elements.remove();
		filer.remove();
	}
}
