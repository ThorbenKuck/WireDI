package com.wiredi.runtime.resources;

import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.ResourceLoader;
import com.wiredi.runtime.resources.builtin.ClassPathResourceProtocolResolver;
import com.wiredi.runtime.resources.builtin.FileSystemResourceProtocolResolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceLoaderTest {

	@Test
	public void testClassPathResource() throws IOException {
		// Arrange
		ResourceLoader resourceLoader = new ResourceLoader();
		resourceLoader.addProtocolResolver(ClassPathResourceProtocolResolver.INSTANCE);

		// Act
		Resource resource = resourceLoader.load("classpath:Test.txt");

		// Assert
		assertThat(resource.exists()).isTrue();
		assertThat(resource.getContentAsString()).isEqualTo("TEST");
	}

	@Test
	public void testFileSystemResource() throws IOException {
		// Arrange
		ResourceLoader resourceLoader = new ResourceLoader();
		resourceLoader.addProtocolResolver(FileSystemResourceProtocolResolver.INSTANCE);

		// Act
		Resource resource = resourceLoader.load("file:src/test/resources/Test.txt");

		// Assert
		assertThat(resource.exists()).isTrue();
		assertThat(resource.getContentAsString()).isEqualTo("TEST");
	}
}
