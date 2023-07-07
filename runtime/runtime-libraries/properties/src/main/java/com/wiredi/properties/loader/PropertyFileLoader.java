package com.wiredi.properties.loader;

import com.wiredi.properties.keys.Key;
import com.wiredi.resources.Resource;

import java.util.List;
import java.util.Map;

public interface PropertyFileLoader {

	Map<Key, String> extract(Resource resource);

	List<String> supportedFileTypes();

}
