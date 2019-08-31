package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.Wire;

import java.util.UUID;

@Wire
public class MetaDependency implements IDependency {

	private final UUID uuid = UUID.randomUUID();

	@Override
	public String id() {
		return uuid.toString();
	}
}
