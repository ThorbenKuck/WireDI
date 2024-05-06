package com.wiredi.processor.tck.domain.properties;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.properties.Entry;
import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertySource;
import com.wiredi.annotations.environment.Resolve;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
@PropertySource(
		value = "classpath:test.properties",
		entries = @Entry(key = "endpoint1", value = "${endpoint.one}")
)
public class RestClient implements TckTestCase {

	@Resolve("${endpoint1}")
	private String endpoint1;

	@Resolve("${endpoint.one}")
	private String endpointOne;

	private final String endpoint1Constructor;
	private final String testPropertyConstructor;
	private final String oneConstructorParameter;
	private final EndpointProperties endpointProperties;

	public RestClient(
			@Resolve("${endpoint1}")
			String endpoint1Constructor,

			@Resolve("${endpoint.one}")
			String endpointOneConstructor,

			@Property(name = "endpoint.one")
			String oneConstructorParameter,
			EndpointProperties endpointProperties
	) {
		this.endpoint1Constructor = endpoint1Constructor;
		this.testPropertyConstructor = endpointOneConstructor;
		this.endpointProperties = endpointProperties;
		this.oneConstructorParameter = oneConstructorParameter;
	}

	@Override
	public Collection<DynamicNode> dynamicTests() {
		return List.of(
				dynamicTest("The constructor property is the same as bound properties", () -> assertThat(oneConstructorParameter).isEqualTo(endpointProperties.getOne()))
		);
	}
}
