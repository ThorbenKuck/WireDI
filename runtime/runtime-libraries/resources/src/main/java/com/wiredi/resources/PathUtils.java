package com.wiredi.resources;

import static com.wiredi.lang.Preconditions.require;

public class PathUtils {

	public static String appendRelative(String path, String relativePath) {
		String targetPath = path;
		String targetRelativePath = relativePath;
		if (!targetPath.endsWith("/")) {
			targetPath = targetPath + "/";
		}
		if (targetRelativePath.startsWith("/")) {
			targetRelativePath = targetRelativePath.substring(1);
		}
		return targetPath + targetRelativePath;
	}

	public static boolean isAbsolute(String path) {
		return path.startsWith("/");
	}

	public static boolean isRelative(String path) {
		return !isAbsolute(path);
	}

	public static String absolute(String path) {
		if (isRelative(path)) {
			return "/" + path;
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
		require(pathElements.length > 0, () -> "At least one path must be provided");
		if (pathElements.length == 1) {
			return pathElements[0];
		}
		StringBuilder stringBuilder = new StringBuilder();

		for (String pathElement : pathElements) {
			if (!stringBuilder.isEmpty()) {
				stringBuilder.append("/");
			}
			stringBuilder.append(PathUtils.relative(pathElement));
		}

		return stringBuilder.toString();
	}
}
