package com.wiredi.resources.builtin;

import com.wiredi.resources.PathUtils;
import com.wiredi.resources.Resource;
import com.wiredi.resources.WritableResource;
import com.wiredi.resources.exceptions.ResourceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemResource implements WritableResource {

	private final String rawPath;
	private final Path path;

	public FileSystemResource(String... path) {
		this(Path.of(PathUtils.join(path)));
	}

	public FileSystemResource(Path path) {
		this.rawPath = path.toAbsolutePath().toString();
		this.path = path;
	}

	@Override
	public boolean exists() {
		return Files.exists(path);
	}

	@Override
	public boolean isFile() {
		return Files.isRegularFile(path);
	}

	@Override
	public URL getURL() {
		try {
			return path.toUri().toURL();
		} catch (MalformedURLException e) {
			throw new ResourceException("Error creating URL", e);
		}
	}

	@Override
	public URI getURI() {
		return path.toUri();
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public InputStream getInputStream() {
		try {
			return Files.newInputStream(path);
		} catch (IOException e) {
			throw new ResourceException("Error opening input stream", e);
		}
	}

	@Override
	public Resource createRelative(String relativePath) {
		return null;
	}

	@Override
	public String getFilename() {
		return rawPath;
	}

	@Override
	public boolean isWritable() {
		return Files.isWritable(path);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return Files.newOutputStream(path);
	}
}
