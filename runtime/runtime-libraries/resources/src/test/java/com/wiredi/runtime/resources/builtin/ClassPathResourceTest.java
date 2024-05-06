package com.wiredi.runtime.resources.builtin;

import com.wiredi.runtime.resources.builtin.ClassPathResource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassPathResourceTest {
	@Test
	public void testThatLoadingAClassPathResourceWorks() throws IOException {
		// Arrange
		ClassPathResource resource = new ClassPathResource("Test.txt");

		// Act
		assertThat(resource.exists()).isTrue();
		assertThat(resource.isFile()).isTrue();
		List<String> lines = Files.readAllLines(resource.getPath());

		// Assert
		assertThat(lines).containsExactly("TEST");
		assertThat(lines.get(0)).isEqualTo(resource.getContentAsString());
	}
}
