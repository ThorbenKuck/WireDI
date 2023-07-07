package com.wiredi.lang.eager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EagerLoadingContext {

	private static final String KEY = "wiredi.eager-loading.threads";
	private static final ExecutorService executorService = Executors.newFixedThreadPool(threads());

	public static <T> void load(Supplier<T> supplier, Consumer<T> resultConsumer) {
		executorService.execute(() -> resultConsumer.accept(supplier.get()));
	}

	private static int threads() {
		String raw = System.getenv().getOrDefault(KEY, System.getProperty(KEY));
		if (raw == null) {
			return Runtime.getRuntime().availableProcessors();
		} else {
			try {
				return Integer.parseInt(raw);
			} catch (NumberFormatException t) {
				final Logger LOGGER = LoggerFactory.getLogger(EagerLoadingContext.class);
				LOGGER.error("Invalid value for property {}: Should be an INT", KEY);
				LOGGER.error("Unexpected exception", t);
				throw new IllegalArgumentException("The property " + KEY + " must be a number!", t);
			}
		}
	}
}
