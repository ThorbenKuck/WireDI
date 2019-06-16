package com.github.thorbenkuck.di.test.inner;

import com.github.thorbenkuck.di.annotations.Nullable;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.test.Foo;
import com.github.thorbenkuck.di.test.IDependency;

import javax.inject.Inject;
import javax.inject.Singleton;

@Wire
@Singleton
public class SuperDi {

	@Inject
	private IDependency iDependency;
	@Inject
	IDependency iDependency2;
	@Inject
	@Nullable
	Foo foo;

	public SuperDi(IDependency iDependency) {
		System.out.println("SuperDi instantiated with Dependency " + iDependency);
	}

	public void foo() {
		System.out.println("Called");
	}
}
