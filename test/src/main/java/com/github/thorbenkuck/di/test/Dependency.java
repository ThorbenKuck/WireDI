package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.Wire;

import javax.inject.Singleton;
import java.util.UUID;

@Wire(to = IDependency.class)
@Singleton
class Dependency implements IDependency {

	private final String id = UUID.randomUUID().toString();

	Dependency() {
		System.out.println("Dependency Instantiated");
	}

	@Override
	public String id() {
		return id;
	}
}
