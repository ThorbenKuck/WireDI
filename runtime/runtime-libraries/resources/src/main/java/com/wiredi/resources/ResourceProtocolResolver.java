package com.wiredi.resources;

import java.util.List;

/**
 * Support for creating custom Resource types.
 * <p>
 * <h2>Example</h2>
 * If you want to construct a custom remote resource, you would need to implement the {@link Resource} interface and
 * then a custom protocol resolver.
 * <p>
 * Let's say you want to have the mentioned remote resource be accessible if the resource name starts with "remote"
 * (i.e. "remote:https://my.url/path/to/resource"), then you will need to provide a ResourceProtocolResolver which
 * contains "remote" in its {@link #types()}
 * <p>
 * Please note: This class should be stateless. It is loaded once globally and hence any state will stay for multiple
 * executions of different WireRepositories.
 */
public interface ResourceProtocolResolver {

    Resource resolve(String path);

    List<String> types();

}
