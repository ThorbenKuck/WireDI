package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.Nullable;
import com.github.thorbenkuck.di.annotations.Origin;
import com.github.thorbenkuck.di.annotations.Resource;
import com.github.thorbenkuck.di.annotations.Wire;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.UUID;

@Wire(to = IDependency.class)
@Singleton
class Dependency implements IDependency {

	@Resource(key = "test.resource")
	private String resource;
	@Resource(key = "not.existing.resource", origin = Origin.NONE)
	private @Nullable String notExisting;
	private final String id = UUID.randomUUID().toString();

	Dependency() {
		System.out.println("Dependency Instantiated");
	}

	@PostConstruct
	public void constructed() {
		if(notExisting != null) {
			throw new IllegalStateException("The property NotExisting should be null!");
		}
	}

	@Override
	public String id() {
		return resource + " " + id;
	}
}
