package com.wiredi.properties;

import com.wiredi.properties.exceptions.PropertyLoadingException;
import com.wiredi.properties.keys.Key;
import com.wiredi.properties.loader.PropertyFileTypeLoader;
import com.wiredi.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PropertyLoader {

	private final Map<String, PropertyFileTypeLoader> propertyFileLoaders = new HashMap<>();

	public PropertyLoader(List<PropertyFileTypeLoader> loaderList) {
		loaderList.forEach(loader -> {
			loader.supportedFileTypes().forEach(supportedFileType -> {
				propertyFileLoaders.computeIfPresent(supportedFileType, (key, existingLoader) -> {
					throw new IllegalStateException("Tried to register " + loader + " for file type " + key + " but " + existingLoader + " was already registered");
				});
				propertyFileLoaders.put(supportedFileType, loader);
			});
		});
	}

	public PropertyLoader(PropertyFileTypeLoader... loaderList) {
		this(Arrays.asList(loaderList));
	}

	public PropertyLoader() {
		// No-Arg
	}

	public void addPropertyFileLoaders(Collection<PropertyFileTypeLoader> loaders) {
		loaders.forEach(this::addPropertyFileLoader);
	}

	public void addPropertyFileLoader(PropertyFileTypeLoader loader) {
		loader.supportedFileTypes().forEach(supportedFileType -> {
			PropertyFileTypeLoader existingPropertyLoader = propertyFileLoaders.get(supportedFileType);
			if (existingPropertyLoader != null) {
				throw new IllegalStateException("Tried to register " + loader + " for file type " + supportedFileType + " but " + existingPropertyLoader + " was already registered");
			}
			propertyFileLoaders.put(supportedFileType, loader);
		});
	}

	public void setPropertyFileLoader(PropertyFileTypeLoader loader) {
		loader.supportedFileTypes().forEach(supportedFileType -> propertyFileLoaders.put(supportedFileType, loader));
	}

	/**
	 * This method will load a resource from the provide resource.
	 * <p>
	 * If the Resource cannot be found, or something is wrong, an exception will be raised.
	 *
	 * @param resource the resource to load
	 * @return a new instance of TypedProperties
	 */
	@NotNull
	public TypedProperties load(@NotNull Resource resource) {
		if (!resource.exists()) {
			throw new PropertyLoadingException(resource.getFilename(), "It does not exist");
		}
		if (!resource.isFile()) {
			throw new PropertyLoadingException(resource.getFilename(), "It is not a file");
		}
		String fileType = resource.fileType()
				.orElseThrow(() -> new PropertyLoadingException(resource.getFilename(), "Could not get file type"));
		PropertyFileTypeLoader propertyFileTypeLoader = Optional.ofNullable(propertyFileLoaders.get(fileType))
				.orElseThrow(() -> new PropertyLoadingException(resource.getFilename(), "No PropertyFileLoader registered for the type " + fileType));

		Map<Key, String> result = propertyFileTypeLoader.extract(resource);
		return TypedProperties.from(result);
	}

	/**
	 * This method will load a resource from the provide resource.
	 * <p>
	 * Contrary to {@link #load(Resource)}, this method will always return an object, even if the requested
	 * resource could not be found.
	 *
	 * @param resource the resource to load
	 * @return a new instance of TypedProperties
	 */
	@NotNull
	public TypedProperties safeLoad(@NotNull Resource resource) {
		TypedProperties typedProperties = new TypedProperties();

		try {
			if (!resource.exists()) {
				return typedProperties;
			}
			if (!resource.isFile()) {
				return typedProperties;
			}

			resource.fileType().ifPresent(fileType -> {
				Optional.ofNullable(propertyFileLoaders.get(fileType)).ifPresent(propertyFileTypeLoader -> {
					Map<Key, String> result = propertyFileTypeLoader.extract(resource);
					typedProperties.setAll(result);
				});
			});
		} catch (Exception ignored) {
			// Ignore any errors, the developer wants a TypedProperties instance, so give him one!
		}
		return typedProperties;
	}
}
