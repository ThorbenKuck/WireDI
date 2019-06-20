package com.github.thorbenkuck.di.test.inner;

import com.github.thorbenkuck.di.Repository;
import com.github.thorbenkuck.di.annotations.Wire;
import com.github.thorbenkuck.di.test.IDependency;
import com.github.thorbenkuck.di.test.NonExisting;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Wire
@Singleton
public class SuperDi {

	@Inject
	private IDependency iDependency;
	@Inject
	private IDependency iDependency2;
	@Inject
	private Repository repository;
	@Inject
	@Nullable
	private NonExisting nonExisting;

	SuperDi(IDependency iDependency) {
		System.out.println("[Started] SuperDi instantiated with Dependency " + iDependency.id());
	}

	public void foo() {
		SuperDiIdentifiableProvider superDiIdentifiableProvider = new SuperDiIdentifiableProvider();
		superDiIdentifiableProvider.instantiate(repository);
		SuperDi superDi = superDiIdentifiableProvider.get();
		System.out.println(this + "==" + superDi + ": " + this.equals(superDi));
		System.out.println("#####");
		System.out.println("Dependency 1 = " + iDependency.id());
		System.out.println("Dependency 2 = " + iDependency2.id());
		System.out.println("NonExisting = " + nonExisting);
		System.out.println("#####");
		iDependency.id();
	}

	@PostConstruct
	private void constructed() {
		System.out.println("[Done] SuperDi constructed");
	}
}
