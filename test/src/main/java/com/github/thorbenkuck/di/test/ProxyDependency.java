package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.Proxy;
import com.github.thorbenkuck.di.annotations.ProxyPreventAnnotations;
import com.github.thorbenkuck.di.annotations.Wire;

import java.util.UUID;

@Proxy(wire = @Wire(to = IProxyDependency.class, lazy = false))
class ProxyDependency implements IProxyDependency {

	private final UUID uuid = UUID.randomUUID();

	@ProxyPreventAnnotations
	@Override
	public void test() {
		System.out.println(uuid.toString());
	}
}
