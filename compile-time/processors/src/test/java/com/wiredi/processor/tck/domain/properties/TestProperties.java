package com.wiredi.processor.tck.domain.properties;

import com.wiredi.annotations.properties.Name;
import com.wiredi.annotations.properties.PropertyBinding;

@PropertyBinding(file = "test.properties")
public class TestProperties {

	private final String endpointOne;

	public TestProperties(@Name("endpoint.one") String endpointOne) {
		this.endpointOne = endpointOne;
	}
}
