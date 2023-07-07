package com.wiredi.runtime.banner;

import com.wiredi.resources.builtin.ClassPathResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Supplier;

public class Banner {

	private static final String BANNER_LOCATION = "banner.txt";
	private static final String DEFAULT_BANNER_LOCATION = "banner.default.txt";

	public String loadBanner() {
		ClassPathResource classPathResource = new ClassPathResource(BANNER_LOCATION);
		if (classPathResource.exists()) {
			try {
				return classPathResource.getContentAsString();
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
			return new ClassPathResource(DEFAULT_BANNER_LOCATION).getContentAsString();
		} catch (IOException e) {
			IllegalStateException illegalStateException = new IllegalStateException("Error loading the default banner!", e);
			if (suppressed != null) {
				illegalStateException.addSuppressed(suppressed);
			}
			throw illegalStateException;
		}
	}

	public void print() {
		print(System.out);
	}

	public void print(PrintStream printStream) {
		printStream.print(loadBanner());
	}
}
