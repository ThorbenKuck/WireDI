package com.wiredi.compiler.processor;

import com.wiredi.compiler.processor.lang.AnnotationProcessorResource;
import com.wiredi.runtime.properties.PropertyLoader;
import com.wiredi.runtime.properties.TypedProperties;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.properties.loader.PropertiesPropertyFileTypeLoader;
import com.wiredi.runtime.properties.loader.YamlPropertyFileTypeLoader;

import javax.annotation.processing.Filer;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessorProperties {

	private static final String FILE_NAME = "wire-di.processor.properties";
	private static final PropertyLoader propertyLoader = new PropertyLoader(new PropertiesPropertyFileTypeLoader(), new YamlPropertyFileTypeLoader());
	private final TypedProperties properties;

	public ProcessorProperties(Filer filer) {
		properties = propertyLoader.safeLoad(new AnnotationProcessorResource(filer, FILE_NAME));
	}

	public void addOptions(Map<String, String> options) {
		this.properties.setAll(
				options.entrySet()
						.stream()
						.collect(Collectors.toMap(it -> Key.format(it.getKey()), Map.Entry::getValue))
		);
	}

	public boolean isEnabled(CompilerPropertyKeys propertyKey) {
		return properties.getBoolean(propertyKey.getRawKey(), toBool(propertyKey.getDefaultValue(), false));
	}

	public boolean isEnabled(Key key, boolean defaultValue) {
		return properties.getBoolean(key, defaultValue);
	}

	public boolean isDisabled(CompilerPropertyKeys propertyKey) {
		return !isEnabled(propertyKey);
	}

	public int getCount(CompilerPropertyKeys propertyKey, int defaultValue) {
		return properties.getInt(propertyKey.getRawKey(), defaultValue);
	}

	public String getName(CompilerPropertyKeys extensionFileName) {
		return properties.require(extensionFileName.getRawKey());
	}

	public String getName(Key key, String defaultValue) {
		return properties.get(key, defaultValue);
	}

	public List<String> getAll(CompilerPropertyKeys propertyKey) {
		return properties.getAll(propertyKey.getRawKey());
	}

	public TypedProperties getSource() {
		return properties;
	}

	private boolean toBool(Object object, boolean def) {
		if (object instanceof Boolean) {
			return (Boolean) object;
		} else if (Objects.equals(object.getClass(), boolean.class)) {
			return (boolean) object;
		} else {
			return def;
		}
	}
}
