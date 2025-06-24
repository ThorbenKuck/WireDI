package com.wiredi.runtime.properties;

import com.wiredi.runtime.properties.exceptions.PropertyLoadingException;
import com.wiredi.runtime.properties.loader.PropertyFileTypeLoader;
import com.wiredi.runtime.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A PropertyLoader is responsible for loading different types of properties.
 * <p>
 * The {@link PropertyFileTypeLoader} class is defining how to load different kind of properties (for example properties,
 * yaml or even json). This class collects and aggregates different types of PropertyFileTypeLoaders.
 * <p>
 * When calling {@link #load}, this class will try to find a {@link PropertyFileTypeLoader} for the type of the resource.
 */
public final class PropertyLoader {

    @NotNull
    private final Map<@NotNull String, @NotNull PropertyFileTypeLoader> propertyFileLoaders = new HashMap<>();

    public PropertyLoader(@NotNull final List<@NotNull PropertyFileTypeLoader> loaderList) {
        loaderList.forEach(loader -> {
            loader.supportedFileTypes().forEach(supportedFileType -> {
                propertyFileLoaders.computeIfPresent(supportedFileType, (key, existingLoader) -> {
                    throw new IllegalStateException("Tried to register " + loader + " for file type " + key + " but " + existingLoader + " was already registered");
                });
                propertyFileLoaders.put(supportedFileType, loader);
            });
        });
    }

    public PropertyLoader(@NotNull final PropertyFileTypeLoader... loaderList) {
        this(Arrays.asList(loaderList));
    }

    public PropertyLoader() {
    }

    public Collection<String> supportedFileTypes() {
        return propertyFileLoaders.keySet();
    }

    /**
     * Add a collection of {@link PropertyFileTypeLoader} to this PropertyLoader.
     * <p>
     * Each individual {@link PropertyFileTypeLoader} will have to respect the {@link #addPropertyFileLoader(PropertyFileTypeLoader)} method
     *
     * @param loaders all loaders to register
     * @throws IllegalStateException if any file type of any loader already
     *                               has a {@link PropertyFileTypeLoader} or is registered twice
     */
    public void addPropertyFileLoaders(@NotNull final Collection<PropertyFileTypeLoader> loaders) {
        loaders.forEach(this::addPropertyFileLoader);
    }

    /**
     * Adds a {@link PropertyFileTypeLoader} to the PropertyLoader, linking it to all the {@link PropertyFileTypeLoader#supportedFileTypes()}.
     * <p>
     * If any file type returned by {@link PropertyFileTypeLoader#supportedFileTypes()} is already linked to another
     * {@link PropertyFileTypeLoader}, an {@link IllegalStateException} will be raised
     *
     * @param loader the loader to register.
     * @throws IllegalStateException if any file type already has a {@link PropertyFileTypeLoader}
     */
    public PropertyLoader addPropertyFileLoader(@NotNull final PropertyFileTypeLoader loader) {
        loader.supportedFileTypes().forEach(supportedFileType -> {
            final PropertyFileTypeLoader existingPropertyLoader = propertyFileLoaders.get(supportedFileType);
            if (existingPropertyLoader != null) {
                throw new IllegalStateException("Tried to register " + loader + " for file type " + supportedFileType + " but " + existingPropertyLoader + " was already registered");
            }
            propertyFileLoaders.put(supportedFileType, loader);
        });

        return this;
    }

    /**
     * Sets a specific {@link PropertyFileTypeLoader}.
     * <p>
     * This method behaves like {@link #addPropertyFileLoader(PropertyFileTypeLoader)}, but will overwrite any
     * pre-registered {@link PropertyFileTypeLoader} for any {@link PropertyFileTypeLoader#supportedFileTypes()}
     * instead of throwing an exception.
     *
     * @param loader the loader to register
     */
    public void setPropertyFileLoader(@NotNull final PropertyFileTypeLoader loader) {
        loader.supportedFileTypes().forEach(supportedFileType -> propertyFileLoaders.put(supportedFileType, loader));
    }

    /**
     * Auto determines all {@link PropertyFileTypeLoader} using the {@link ServiceLoader}.
     *
     * @return this
     */
    @NotNull
    public PropertyLoader autoconfigure() {
        ServiceLoader.load(PropertyFileTypeLoader.class)
                .stream()
                .forEach(provider -> {
                    try {
                        addPropertyFileLoader(provider.get());
                    } catch (Throwable ignored) {
                    }
                });

        return this;
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
    public TypedProperties load(@NotNull final Resource resource) {
        @NotNull final String fileType = resource.fileType()
                .orElseThrow(() -> new PropertyLoadingException(resource.getFilename(), "Could not get file type"));
        @NotNull final PropertyFileTypeLoader propertyFileTypeLoader = Optional.ofNullable(propertyFileLoaders.get(fileType))
                .orElseThrow(() -> new PropertyLoadingException(resource.getFilename(), "No PropertyFileLoader registered for the type " + fileType));

        @NotNull final Map<Key, String> result = propertyFileTypeLoader.extract(resource);
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
    public TypedProperties safeLoad(@NotNull final Resource resource) {
        @NotNull final TypedProperties typedProperties = new TypedProperties();

        try {
            if (!resource.exists()) {
                return typedProperties;
            }
            if (!resource.isFile()) {
                return typedProperties;
            }

            resource.fileType()
                    .flatMap(fileType -> Optional.ofNullable(propertyFileLoaders.get(fileType)))
                    .ifPresent(propertyFileTypeLoader -> {
                        @NotNull final Map<Key, String> result = propertyFileTypeLoader.extract(resource);
                        typedProperties.setAll(result);
                    });
        } catch (@NotNull final Exception ignored) {
            // Ignore any errors, the developer wants a TypedProperties instance, so give him one!
        }
        return typedProperties;
    }

    public void clear() {
        propertyFileLoaders.clear();
    }
}
