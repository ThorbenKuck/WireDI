package com.wiredi.resources;

import com.wiredi.resources.builtin.ClassPathResourceProtocolResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceLoaderTest {

	@Test
	public void test() throws IOException {
		// Arrange
		ResourceLoader resourceLoader = new ResourceLoader();
		resourceLoader.addProtocolResolver(new ClassPathResourceProtocolResolver());

		// Act
		Resource resource = resourceLoader.load("classpath:Test.txt");

		// Assert
		assertThat(resource.exists()).isTrue();
		assertThat(resource.getContentAsString()).isEqualTo("TEST");
	}

}