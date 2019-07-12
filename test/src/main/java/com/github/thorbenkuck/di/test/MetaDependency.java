package com.github.thorbenkuck.di.test;

import java.util.UUID;

@CollectForWire
public class MetaDependency implements IMetaDependency {

	private final UUID uuid = UUID.randomUUID();

	@Override
	public String id() {
		return uuid.toString();
	}
}
