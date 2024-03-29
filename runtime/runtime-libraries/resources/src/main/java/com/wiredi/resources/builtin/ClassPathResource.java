package com.wiredi.resources.builtin;

import com.wiredi.resources.PathUtils;
import com.wiredi.resources.Resource;
import com.wiredi.resources.exceptions.ResourceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassPathResource implements Resource {

	private final String path;
	private final ClassLoader classLoader;

	public ClassPathResource(@NotNull String path) {
		this(path, Thread.currentThread().getContextClassLoader());
	}

	public ClassPathResource(@NotNull String path, @Nullable ClassLoader classLoader) {
		this.path = path;
		this.classLoader = classLoader;
	}

	@Override
	public boolean exists() {
		return this.classLoader.getResource(this.path) != null && Files.exists(getPath());
	}

	@Override
	public boolean isFile() {
		return Files.isRegularFile(getPath());
	}

	@Override
	public @NotNull URL getURL() {
		if (this.classLoader != null) {
			URL resource = this.classLoader.getResource(this.path);
			if (resource == null) {
				throw new ResourceException("Could not create an URL for the resource " + path);
			}
			return resource;
		} else {
			return ClassLoader.getSystemResource(this.path);
		}
	}

	@Override
	public @NotNull URI getURI() {
		try {
			return getURL().toURI();
		} catch (URISyntaxException e) {
			throw new ResourceException("Error creating URI", e);
		}
	}

	@Override
	public @NotNull Path getPath() {
		return Path.of(getURI());
	}

	@Override
	public @NotNull InputStream getInputStream() {
		InputStream is;
		if (this.classLoader != null) {
			is = this.classLoader.getResourceAsStream(this.path);
		} else {
			is = ClassLoader.getSystemResourceAsStream(this.path);
		}
		if (is == null) {
			throw new ResourceException(path + " cannot be opened because it does not exist");
		}
		return is;
	}

	@Override
	public @NotNull Resource createRelative(@NotNull String relativePath) {
		if(isFile()) {
			throw new ResourceException("Cannot create a relative path from file " + path);
		}
		return new ClassPathResource(PathUtils.appendRelative(path, relativePath));
	}

	@Override
	public String getFilename() {
		return path;
	}
}
