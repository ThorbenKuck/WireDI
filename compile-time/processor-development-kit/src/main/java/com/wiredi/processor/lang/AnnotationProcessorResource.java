package com.wiredi.processor.lang;

import com.wiredi.lang.values.SafeReference;
import com.wiredi.resources.PathUtils;
import com.wiredi.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class AnnotationProcessorResource implements Resource {

	private final Filer filer;
	private final String path;
	private SafeReference<FileObject> fileObject;

	public AnnotationProcessorResource(Filer filer, String path) {
		this.filer = filer;
		this.path = path;

	}

	public SafeReference<FileObject> getFileObject() {
		if (fileObject == null) {
			try {
				FileObject resource = filer.getResource(StandardLocation.CLASS_PATH, "", path);
				fileObject = new SafeReference<>(resource);
			} catch (IOException e) {
				fileObject = new SafeReference<>();
			}
		}
		return fileObject;
	}

	public Reader openReader() throws IOException {
		return getFileObject().mapIfPresent(fileObject -> fileObject.openReader(true));
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
	@Nullable
	public URL getURL() {
		try {
			return getFileObject().mapIfPresent(it -> it.toUri().toURL());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	@Nullable
	public URI getURI() {
		return getFileObject().mapIfPresent(FileObject::toUri);
	}

	@Override
	@NotNull
	public Path getPath() {
		return Optional.ofNullable(getURI())
				.map(Path::of)
				.orElseGet(() -> Path.of(path));
	}

	@Override
	public InputStream getInputStream() {
		InputStream inputStream = null;
		try {
			inputStream = getFileObject().mapIfPresent(FileObject::openInputStream);
		} catch (IOException ignored) {
		}
		return inputStream;
	}

	@Override
	public Resource createRelative(String relativePath) {
		return new AnnotationProcessorResource(
				filer,
				PathUtils.join(this.path, relativePath)
		);
	}

	@Override
	@Nullable
	public String getFilename() {
		return getFileObject().mapIfPresent(FileObject::getName);
	}
}
