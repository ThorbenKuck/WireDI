package com.wiredi.compiler.processor.lang.utils;

import jakarta.inject.Inject;

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
public class ServicesFiles {

	private final Filer filer;

	public static final String SERVICES_PATH = "META-INF/services";

	private ServicesFiles(Filer filer) {
		this.filer = filer;
	}

	/**
	 * Returns an absolute path to a service file given the class
	 * name of the service.
	 *
	 * @param serviceName not {@code null}
	 * @return SERVICES_PATH + serviceName
	 */
	public String getPathToServiceFile(String serviceName) {
		return SERVICES_PATH + "/" + serviceName;
	}


	public Set<String> readServiceFile(String serviceName) throws IOException {
		return readServiceFile(serviceName, StandardLocation.CLASS_OUTPUT);
	}

	public void addServiceImplementation(String serviceName, String implementation) throws IOException {
        addServiceImplementation(serviceName, implementation, StandardLocation.CLASS_OUTPUT);
	}

	public void writeServiceFile(String serviceName, Collection<String> services) throws IOException {
        writeServiceFile(serviceName, services, StandardLocation.CLASS_OUTPUT);
	}

    public void addServiceImplementation(String serviceName, String implementation, StandardLocation standardLocation) throws IOException {
        Set<String> services = readServiceFile(serviceName, standardLocation);
        if(!services.contains(serviceName)) {
            services.add(serviceName);
            writeServiceFile(serviceName, services, standardLocation);
        }
    }

    public Set<String> readServiceFile(String serviceName, StandardLocation standardLocation) throws IOException {
        FileObject resource = filer.getResource(standardLocation, "", getPathToServiceFile(serviceName));
        return readServiceFile(resource.openInputStream());
    }

	public void writeServiceFile(String serviceName, Collection<String> services, StandardLocation standardLocation) throws IOException {
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
