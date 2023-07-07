package com.wiredi.properties;

import com.wiredi.properties.keys.Key;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class TypedPropertiesTest {

	@Test
	public void test() throws Exception {
		// Arrange
		Key propertyKey = Key.just("property");
		Key systemKey = Key.just("system");
//		String environmentKey = "env";
		TypedProperties typedProperties = new TypedProperties();
		typedProperties.set(propertyKey, propertyKey.value());
		System.setProperty(systemKey.value(), systemKey.value());
		typedProperties.set(systemKey, "NOT-" + systemKey);
//		setEnvironmentVariable(environmentKey, environmentKey);

		// Act
		String propertyProperty = typedProperties.require(propertyKey);
		String systemProperty = typedProperties.require(systemKey);
//		String envProperty = typedProperties.require(environmentKey);

		// Assert
		assertThat(propertyKey.value()).isEqualTo(propertyProperty);
		assertThat(systemKey.value()).isEqualTo(systemProperty);
//		assertThat(environmentKey).isEqualTo(envProperty);
	}
}