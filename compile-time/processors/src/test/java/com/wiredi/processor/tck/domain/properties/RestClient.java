package com.wiredi.processor.tck.domain.properties;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.properties.Entry;
import com.wiredi.annotations.properties.PropertySource;
import com.wiredi.annotations.properties.Resolve;

@Wire
@PropertySource(
		value = "test.properties",
		entries = @Entry(key = "endpoint1", value = "${endpoint.one}")
)
public class RestClient {

	@Resolve("${endpoint1}")
	private String endpoint1;

	@Resolve("${endpoint.one}")
	private String endpointOne;

	private final String endpoint1Constructor;
	private final String testPropertyConstructor;
	private final EndpointProperties endpointProperties;

	public RestClient(
			@Resolve("${endpoint1}")
			String endpoint1Constructor,

			@Resolve("${endpoint.one}")
			String endpointOneConstructor,

			EndpointProperties endpointProperties
	) {
		this.endpoint1Constructor = endpoint1Constructor;
		this.testPropertyConstructor = endpointOneConstructor;
		this.endpointProperties = endpointProperties;
	}
}
