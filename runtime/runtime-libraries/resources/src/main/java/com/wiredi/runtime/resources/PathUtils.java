package com.wiredi.runtime.resources;

import java.nio.file.*;
import java.util.Optional;

import static com.wiredi.runtime.lang.Preconditions.is;

public class PathUtils {

	private static final String fileSeparator = FileSystems.getDefault().getSeparator();

	public static Optional<Path> toPath(String raw) {
		try {
			return Optional.of(Paths.get(raw));
		} catch (InvalidPathException | NullPointerException ex) {
			return Optional.empty();
		}
	}

	public static String concat(String path, String relativePath) {
		String targetPath = path;
		String targetRelativePath = relativePath;
		if (!targetPath.endsWith(fileSeparator)) {
			targetPath = targetPath + fileSeparator;
		}
		if (targetRelativePath.startsWith(fileSeparator)) {
			targetRelativePath = targetRelativePath.substring(1);
		}
		return targetPath + targetRelativePath;
	}

	public static boolean isAbsolute(String path) {
		return path.startsWith(fileSeparator);
	}

	public static boolean isRelative(String path) {
		return !isAbsolute(path);
	}

	public static String absolute(String path) {
		if (isRelative(path)) {
			return fileSeparator + path;
		} else {
			return path;
		}
	}

	public static String relative(String path) {
		if (isAbsolute(path)) {
			return path.substring(1);
		} else {
			return path;
		}
	}

	public static String join(String... pathElements) {
		is(pathElements.length > 0, () -> "At least one path must be provided");
		if (pathElements.length == 1) {
			return pathElements[0];
		}
		StringBuilder stringBuilder = new StringBuilder();

		for (String pathElement : pathElements) {
			if (!stringBuilder.isEmpty()) {
				stringBuilder.append(fileSeparator);
			}
			stringBuilder.append(PathUtils.relative(pathElement));
		}

		return stringBuilder.toString();
	}
}
