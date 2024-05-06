package com.wiredi.compiler.processor.lang;

import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.values.SafeReference;
import com.wiredi.runtime.resources.PathUtils;
import com.wiredi.runtime.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class AnnotationProcessorResource implements Resource {

	private static final Logger logger = Logger.get(AnnotationProcessorResourceResolver.class);
	public static final Deque<JavaFileManager.Location> DEFAULT_LOCATIONS_TO_CHECK = new LinkedBlockingDeque<>();

	static {
		DEFAULT_LOCATIONS_TO_CHECK.addLast(StandardLocation.ANNOTATION_PROCESSOR_PATH);
		DEFAULT_LOCATIONS_TO_CHECK.addLast(StandardLocation.CLASS_PATH);
		DEFAULT_LOCATIONS_TO_CHECK.addLast(StandardLocation.PLATFORM_CLASS_PATH);
	}

	private final Filer filer;
	private final String path;
	private final Deque<JavaFileManager.Location> locationsToCheck;
	private SafeReference<FileObject> fileObject;

	public AnnotationProcessorResource(Filer filer, String path) {
		this(filer, path, new LinkedBlockingDeque<>(DEFAULT_LOCATIONS_TO_CHECK));
	}

	public AnnotationProcessorResource(Filer filer, String path, List<JavaFileManager.Location> locations) {
		this(filer, path, new LinkedBlockingDeque<>(locations));
	}

	public AnnotationProcessorResource(Filer filer, String path, Deque<JavaFileManager.Location> locations) {
		this.filer = filer;
		this.path = path;
		this.locationsToCheck = locations;
	}

	public AnnotationProcessorResource addLocation(JavaFileManager.Location location) {
		this.locationsToCheck.addLast(location);
		return this;
	}

	public AnnotationProcessorResource addPriorityLocation(JavaFileManager.Location location) {
		this.locationsToCheck.addFirst(location);
		return this;
	}

	private FileObject tryLoadFileObjectFromLocation(JavaFileManager.Location location) {
        try {
			FileObject resource = filer.getResource(location, "", path);
			if (resource != null && resource.getLastModified() != 0) {
				logger.debug(() -> "Found " + path + " on location " + location);
				return resource;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.debug(() -> "File " + path + " could not be found on location " + location);
            return null;
        }
    }

	public SafeReference<FileObject> getFileObject() {
		if (fileObject == null) {
			while (locationsToCheck.peekFirst() != null) {
				JavaFileManager.Location next = locationsToCheck.pollFirst();
				FileObject targetFileObject = tryLoadFileObjectFromLocation(next);
				if (targetFileObject != null) {
					logger.debug(() -> "Found the resource " + path + " in location " + next);
					this.fileObject = new SafeReference<>(targetFileObject);
					return this.fileObject;
				}
			}
			logger.debug(() -> "Error resolving " + path + " from all known standard locations");
			fileObject = new SafeReference<>();
		}

		return fileObject;
	}

	@Override
	public Reader openReader() {
        try {
            return getFileObject().mapAndGetIfPresent(fileObject -> fileObject.openReader(true));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

	@Override
	public boolean exists() {
		return getFileObject().isPresent();
	}

	@Override
	public boolean isFile() {
		return Files.isRegularFile(getPath());
	}

	@Override
    public @NotNull URL getURL() {
		try {
			return getFileObject()
					.mapIfPresent(it -> it.toUri().toURL())
					.getOrThrow(() -> new IllegalStateException("The resource " + path + " does not exist"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public @NotNull URI getURI() {
		return getFileObject()
				.mapIfPresent(FileObject::toUri)
				.getOrThrow(() -> new IllegalStateException("The resource " + path + " does not exist"));
	}

	@Override
	@NotNull
	public Path getPath() {
		return getFileObject()
				.mapIfPresent(it -> Path.of(it.toUri()))
				.orElse(() -> Path.of(path));
	}

	@Override
	public @NotNull InputStream getInputStream() {
		InputStream inputStream = null;
		try {
			inputStream = getFileObject().mapAndGetIfPresent(FileObject::openInputStream);
		} catch (IOException ignored) {
		}
		if (inputStream == null) {
			throw new IllegalStateException("The resource " + path + " does not exist");
		}
		return inputStream;
	}

	@Override
	public @NotNull Resource createRelative(@NotNull String relativePath) {
		return new AnnotationProcessorResource(
				filer,
				PathUtils.join(this.path, relativePath)
		);
	}

	@Override
	@Nullable
	public String getFilename() {
		return getFileObject().mapAndGetIfPresent(FileObject::getName);
	}
}
