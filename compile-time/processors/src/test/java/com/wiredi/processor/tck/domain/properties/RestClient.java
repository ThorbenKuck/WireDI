package com.wiredi.processor.tck.domain.properties;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.properties.PropertySource;

@Wire
@PropertySource(
		value = "test.properties",
		entries = @PropertySource.KeyValue(key = "endpoint1", value = "${endpoint.one}")
)
public class RestClient {



}
