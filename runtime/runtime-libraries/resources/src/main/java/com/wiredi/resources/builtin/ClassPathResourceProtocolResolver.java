package com.wiredi.resources.builtin;

import com.wiredi.resources.ResourceProtocolResolver;
import com.wiredi.resources.Resource;

import java.util.List;

public class ClassPathResourceProtocolResolver implements ResourceProtocolResolver {
	@Override
	public Resource resolve(String path) {
		return new ClassPathResource(path);
	}

	@Override
	public List<String> types() {
		return List.of("classpath");
	}
}
