package com.wiredi.runtime.banner;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.builtin.ClassPathResource;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class Banner {

	public static final Key BANNER_ENRICHMENT_PROPERTY = Key.just("banner.enrich");
	public static final Key BANNER_MODE_PROPERTY = Key.just("banner.display-mode");
	public static final Key BANNER_LOCATION = Key.just("banner.location");
	public static final String CLASSPATH_BANNER_LOCATION = "classpath:banner.txt";
	private static final Logging logger = Logging.getInstance(Banner.class);

	private final Environment environment;
	private final Value<List<String>> bannerContent = Value.lazy(this::loadBanner);

	public Banner(Environment environment) {
		this.environment = environment;
	}

	public void print() {
		BannerMode bannerMode = environment.getProperty(BANNER_MODE_PROPERTY, BannerMode.class, BannerMode.CONSOLE);

		if (bannerMode == BannerMode.CONSOLE) {
			System.out.print(String.join("", bannerContent.get()));
		}
		if (bannerMode == BannerMode.LOGGER) {
			bannerContent.get().forEach(logger::info);
		}
	}

	private List<String> loadBanner() {
		String property = environment.getProperty(BANNER_LOCATION, CLASSPATH_BANNER_LOCATION);
		Resource resource = environment.loadResource(property);
		if (resource.doesNotExist()) {
			throw new ExceptionInInitializerError("Unable to load banner from " + resource);
		}

		try {
			return enrichWithProperties(resource);
		} catch (final IOException e) {
			ExceptionInInitializerError error = new ExceptionInInitializerError("Unable to load banner from " + resource);
			error.addSuppressed(e);
			throw error;
		}
	}

	private List<String> enrichWithProperties(
			@NotNull final Resource resource
	) throws IOException {
		Boolean enrich = environment.getProperty(BANNER_ENRICHMENT_PROPERTY, true);
		List<String> result = new ArrayList<>();
		try (BufferedReader bufferedInputStream = new BufferedReader(resource.openReader())) {
			while (bufferedInputStream.ready()) {
				String line = bufferedInputStream.readLine();
				if (enrich) {
					line = environment.resolve(line);
				}
				result.add(line + System.lineSeparator());
			}
		}

		return result;
	}
}
