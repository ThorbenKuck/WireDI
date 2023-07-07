package com.wiredi.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public interface Resource {

	boolean exists();

	boolean isFile();

	URL getURL();

	URI getURI();

	Path getPath();

	InputStream getInputStream();

	Resource createRelative(String relativePath);

	String getFilename();

	default void ifExists(Consumer<Resource> resourceConsumer) {
		if (exists()) {
			resourceConsumer.accept(this);
		}
	}

	default Optional<String> fileType() {
		String filename = getFilename();
		return Optional.ofNullable(filename)
				.filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}


	default byte[] getContentAsByteArray() throws IOException {
		return getInputStream().readAllBytes();
	}

	default String getContentAsString(Charset charset) throws IOException {
		return new String(getInputStream().readAllBytes(), charset);
	}

	default String getContentAsString() throws IOException {
		return getContentAsString(Charset.defaultCharset());
	}

	default boolean doesNotExist() {
		return !exists();
	}
}
