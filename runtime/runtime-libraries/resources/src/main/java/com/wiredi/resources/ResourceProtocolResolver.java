package com.wiredi.resources;

import java.util.List;

public interface ResourceProtocolResolver {

	Resource resolve(String path);

	List<String> types();

}
