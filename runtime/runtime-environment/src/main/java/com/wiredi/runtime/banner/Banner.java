package com.wiredi.runtime.banner;

import com.wiredi.environment.Environment;
import com.wiredi.properties.keys.Key;
import com.wiredi.resources.Resource;
import com.wiredi.resources.builtin.ClassPathResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;

public class Banner {

	public static final String BANNER_LOCATION = "banner.txt";
	public static final String DEFAULT_BANNER_LOCATION = "banner.default.txt";
	public static final Key BANNER_ENRICHMENT_PROPERTY = Key.just("banner.enrich");
	public static final Key SHOW_BANNER_PROPERTY = Key.just("show-banner");
	public static final Resource DEFAULT_BANNER_RESOURCE = new ClassPathResource(DEFAULT_BANNER_LOCATION);

	@Nullable
	private final Environment environment;

	@NotNull
	private final PrintStream printStream;

	public Banner(@Nullable Environment environment, @NotNull PrintStream out) {
		this.environment = environment;
		this.printStream = out;
	}

	public Banner(@Nullable Environment environment) {
		this(environment, System.out);
	}

	public Banner(@NotNull PrintStream out) {
		this(null, out);
	}

	public Banner() {
		this(null, System.out);
	}

	public String loadBanner() {
		ClassPathResource classPathResource = new ClassPathResource(BANNER_LOCATION);
		if (classPathResource.exists()) {
			try {
				return enrichWithProperties(classPathResource);
			} catch (IOException e) {
				return loadDefaultBanner(e);
			}
		} else {
			return loadDefaultBanner();
		}
	}

	public String loadDefaultBanner() {
		return loadDefaultBanner(null);
	}

	public String loadDefaultBanner(@Nullable Throwable suppressed) {
		try {
			return enrichWithProperties(DEFAULT_BANNER_RESOURCE);
		} catch (IOException e) {
			IllegalStateException illegalStateException = new IllegalStateException("Error loading the default banner!", e);
			if (suppressed != null) {
				illegalStateException.addSuppressed(suppressed);
			}
			throw illegalStateException;
		}
	}

	private String enrichWithProperties(Resource resource) throws IOException {
		String banner = resource.getContentAsString();
		if (environment != null && environment.properties().getBoolean(BANNER_ENRICHMENT_PROPERTY, true)) {
			return environment.resolve(banner);
		}

		return banner;
	}

	public void print() {
		if (environment != null && environment.properties().getBoolean(SHOW_BANNER_PROPERTY, true)) {
			printStream.print(loadBanner());
		}
	}
}
