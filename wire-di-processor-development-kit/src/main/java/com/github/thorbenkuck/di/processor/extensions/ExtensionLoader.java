package com.github.thorbenkuck.di.processor.extensions;

import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.ProcessorContext;
import com.github.thorbenkuck.di.processor.ProcessorProperties;
import com.github.thorbenkuck.di.processor.PropertyKeys;
import org.jetbrains.annotations.NotNull;

import javax.tools.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class ExtensionLoader {

	private static final String EXTENSION_FILE_NAME = ProcessorProperties.getName(PropertyKeys.EXTENSION_FILE_NAME);
	private static final String SERVICE_FILE = "META-INF/services/" + WireExtension.class.getName();
	private final List<Class<? extends WireExtension>> registeredExtensions;

	public static ExtensionLoader load() {
		return load(ClassLoader.getSystemClassLoader());
	}

	public static ExtensionLoader load(ClassLoader classLoader) {
		InputStream inputStream;
		try {
			FileObject resource = ProcessorContext.getFiler().getResource(StandardLocation.CLASS_PATH, "", SERVICE_FILE);
			inputStream = resource.openInputStream();
			if(inputStream == null) {
				Logger.debug("No WireDI extension file found");
				return new ExtensionLoader(Collections.emptyList());
			}
			Logger.debug("Found WireDI extension file");
		} catch (IOException e) {
			Logger.debug("No WireDI extension file found");
			return new ExtensionLoader(Collections.emptyList());
		}

		List<String> classNames = new ArrayList<>();

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			reader.lines()
					.filter(it -> !it.trim().startsWith("#"))
					.forEach(classNames::add);
		} catch (IOException e) {
			throw new IllegalStateException("Error while reading WireDI extension file", e);
		}

		List<String> illegalClassNames = new ArrayList<>();
		List<Class<?>> registeredClasses = new ArrayList<>();
		List<Class<? extends WireExtension>> extensionClasses = new ArrayList<>();

		Logger.debug("Starting to process %s set file names", Integer.toString(classNames.size()));
		for (String name : classNames) {
			try {
				System.out.println(name);
				String packageName = name.substring(0, name.lastIndexOf("."));
				String fileName = name.substring(name.lastIndexOf(".") + 1) + ".java";
				Logger.info("Package: " + packageName);
				Logger.info("FileName: " + fileName);
				FileObject resource = ProcessorContext.getFiler().getResource(StandardLocation.SOURCE_PATH, packageName, fileName);
				new URLClassLoader(new URL[] { resource.toUri().toURL() });
				System.out.println(resource);
				Logger.info(resource.toUri().toString());
				Logger.debug("ClassLoader: %s", classLoader);
				Class<?> classResult = Class.forName(name, false, classLoader);
				registeredClasses.add(classResult);
			} catch (ClassNotFoundException e) {
				illegalClassNames.add("ClassNotFound: " + name);
			} catch (Throwable e) {
				Logger.catching(e);
				illegalClassNames.add(e.getClass().getSimpleName() + ": " + name);
			}
		}

		for (Class<?> registeredClass : registeredClasses) {
			if(registeredClass.isAssignableFrom(WireExtension.class)) {
				extensionClasses.add((Class<? extends WireExtension>) registeredClass);
				Logger.debug("Found extension with name %s", registeredClass.getName());
			} else {
				illegalClassNames.add("Not subtype of WireExtension: " + registeredClass.getName());
			}
		}

		if(!illegalClassNames.isEmpty()) {
			StringBuilder builder = new StringBuilder("Illegal entries found in WireDI extension file:").append(System.lineSeparator());
			illegalClassNames.forEach(name -> builder.append(" - ").append(name).append(System.lineSeparator()));
			throw new IllegalStateException(builder.toString());
		}

		return new ExtensionLoader(extensionClasses);
	}

	public ExtensionLoader(@NotNull List<Class<? extends WireExtension>> registeredExtensions) {
		this.registeredExtensions = registeredExtensions;
	}

	public void forEach(Consumer<WireExtension> consumer) {
		for (Class<? extends WireExtension> registeredExtension : registeredExtensions) {
			try {
				WireExtension instance = registeredExtension.newInstance();
				consumer.accept(instance);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public Collection<WireExtension> collect() {
		ArrayList<WireExtension> wireExtensions = new ArrayList<>();
		forEach(wireExtensions::add);
		return wireExtensions;
	}
}
