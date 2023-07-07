package com.wiredi.resources.builtin;

import com.wiredi.resources.ResourceProtocolResolver;
import com.wiredi.resources.Resource;

import java.util.List;

public class FileSystemResourceProtocolResolver implements ResourceProtocolResolver {
	@Override
	public Resource resolve(String path) {
		return new FileSystemResource(path);
	}

	@Override
	public List<String> types() {
		return List.of("file");
	}
}
