package com.github.thorbenkuck.di.processor.utils;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WiredProperties {

	public static final String REGISTERED_PROPERTIES_PATH = "META-INF/autoload-properties";
	private static final Predicate<String> IS_COMMENT = Pattern.compile("[ ]*[#]+").asPredicate();

	private WiredProperties() {
	}

	public static Set<String> readRegisteredProperties(Filer filer) {
		try {
			FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", REGISTERED_PROPERTIES_PATH);
			return readRegisteredProperties(resource.openInputStream());
		} catch (IOException e) {
            return new HashSet<>();
		}
	}

	/**
	 * Reads the set of service classes from a service file.
	 *
	 * @param input not {@code null}. Closed after use.
	 * @return a not {@code null Set} of service class names.
	 * @throws IOException
	 */
	public static Set<String> readRegisteredProperties(InputStream input) throws IOException {
		HashSet<String> serviceClasses = new HashSet<>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(input, UTF_8))) {
			String line;
			while ((line = r.readLine()) != null) {
				if (!IS_COMMENT.test(line)) {
					line = line.trim();
					if (!line.isEmpty()) {
						serviceClasses.add(line);
					}
				}
			}
			return serviceClasses;
		}
	}

	/**
	 * Writes the set of service class names to a service file.
	 *
	 * @param output   not {@code null}.
	 * @param services a not {@code null Collection} of service class names.
	 * @throws IOException
	 */
	static void writeRegisteredProperties(Collection<String> services, OutputStream output) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, UTF_8))) {
			for (String service : services) {
				writer.write(service);
				writer.newLine();
			}
			writer.flush();
		}
	}
}
