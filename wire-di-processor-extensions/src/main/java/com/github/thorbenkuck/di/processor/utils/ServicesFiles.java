package com.github.thorbenkuck.di.processor.utils;

import com.github.thorbenkuck.di.processor.ProcessorContext;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A helper class for reading and writing Services files.
 */
final class ServicesFiles {
	public static final String SERVICES_PATH = "META-INF/services";

	private ServicesFiles() {
	}

	/**
	 * Returns an absolute path to a service file given the class
	 * name of the service.
	 *
	 * @param serviceName not {@code null}
	 * @return SERVICES_PATH + serviceName
	 */
	public static String getPathToServiceFile(String serviceName) {
		return SERVICES_PATH + "/" + serviceName;
	}

	public static Set<String> readServiceFile(String serviceName) throws IOException {
		return readServiceFile(serviceName, ProcessorContext.getFiler());
	}

	public static Set<String> readServiceFile(String serviceName, Filer filer) throws IOException {
		return readServiceFile(serviceName, filer, StandardLocation.CLASS_OUTPUT);
	}

	public static void addServiceImplementation(String serviceName, String implementation) throws IOException {
        addServiceImplementation(serviceName, implementation, ProcessorContext.getFiler());
	}

	public static void addServiceImplementation(String serviceName, String implementation, Filer filer) throws IOException {
        addServiceImplementation(serviceName, implementation, ProcessorContext.getFiler(), StandardLocation.CLASS_OUTPUT);
	}

	public static void writeServiceFile(String serviceName, Collection<String> services) throws IOException {
        writeServiceFile(serviceName, services, ProcessorContext.getFiler());
	}

	public static void writeServiceFile(String serviceName, Collection<String> services, Filer filer) throws IOException {
        writeServiceFile(serviceName, services, filer, StandardLocation.CLASS_OUTPUT);
	}

    public static void addServiceImplementation(String serviceName, String implementation, Filer filer, StandardLocation standardLocation) throws IOException {
        Set<String> services = readServiceFile(serviceName, filer, standardLocation);
        if(!services.contains(serviceName)) {
            services.add(serviceName);
            writeServiceFile(serviceName, services, filer, standardLocation);
        }
    }

    public static Set<String> readServiceFile(String serviceName, Filer filer, StandardLocation standardLocation) throws IOException {
        FileObject resource = filer.getResource(standardLocation, "", getPathToServiceFile(serviceName));
        return readServiceFile(resource.openInputStream());
    }

	public static void writeServiceFile(String serviceName, Collection<String> services, Filer filer, StandardLocation standardLocation) throws IOException {
        FileObject resource = filer.getResource(standardLocation, "", getPathToServiceFile(serviceName));
        writeServiceFile(services, resource.openOutputStream());
    }

	/**
	 * Reads the set of service classes from a service file.
	 *
	 * @param input not {@code null}. Closed after use.
	 * @return a not {@code null Set} of service class names.
	 * @throws IOException
	 */
	public static Set<String> readServiceFile(InputStream input) throws IOException {
		HashSet<String> serviceClasses = new HashSet<String>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(input, UTF_8))) {
			// TODO(gak): use CharStreams

			String line;
			while ((line = r.readLine()) != null) {
				int commentStart = line.indexOf('#');
				if (commentStart >= 0) {
					line = line.substring(0, commentStart);
				}
				line = line.trim();
				if (!line.isEmpty()) {
					serviceClasses.add(line);
				}
			}
			return serviceClasses;
		}
	}

	/**
	 * Writes the set of service class names to a service file.
	 *
	 * @param output   not {@code null}. Not closed after use.
	 * @param services a not {@code null Collection} of service class names.
	 * @throws IOException
	 */
	public static void writeServiceFile(Collection<String> services, OutputStream output) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, UTF_8))) {
			for (String service : services) {
				writer.write(service);
				writer.newLine();
			}
			writer.flush();
		}
	}
}
