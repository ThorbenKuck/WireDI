package com.wiredi;

import com.wiredi.compiler.CallerAware;
import com.wiredi.compiler.Injector;
import com.wiredi.compiler.InjectorConfiguration;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InjectorTest {

	@Test
	public void verifyThatByDefaultAllClassesAreSingleton() {
		Injector injector = new Injector();
		A a = injector
				.bind(I.class).toType(IImpl.class)
				.get(A.class);

		assertThat(a).isNotNull().isSameAs(injector.get(A.class));
		assertThat(a.b).isNotNull().isSameAs(injector.get(B.class));
		assertThat(a.b.c()).isNotNull().isSameAs(injector.get(C.class));
		assertThat(a.b.c().d).isNotNull().isSameAs(injector.get(D.class));
		assertThat(a.b.c().d.i()).isNotNull().isSameAs(injector.get(I.class)).isSameAs(a.b.c().i);
		assertThat(a.b.c().i).isSameAs(injector.get(I.class));
		assertThat(a.b.c().j).isSameAs(injector.get(J.class));
	}

	@Test
	public void verifyThatSingletonCanBeChangedToMultiton() {
		Injector injector = new Injector(new InjectorConfiguration().singletonFirst(false));
		A a = injector
				.bind(I.class).toType(IImpl.class)
				.get(A.class);

		assertThat(a).isNotNull().isNotSameAs(injector.get(A.class));
		assertThat(a.b).isNotNull().isNotSameAs(injector.get(B.class));
		assertThat(a.b.c()).isNotNull().isNotSameAs(injector.get(C.class));
		assertThat(a.b.c().d).isNotNull().isNotSameAs(injector.get(D.class));
		assertThat(a.b.c().d.i()).isNotNull().isNotSameAs(injector.get(I.class)).isNotSameAs(a.b.c().i);
		assertThat(a.b.c().i).isNotSameAs(injector.get(I.class));
		assertThat(a.b.c().j).isNotSameAs(injector.get(J.class));
	}

	@Test
	public void verifyThatCallerAwareIsResolvedCorrectly() {
		Injector injector = new Injector(new InjectorConfiguration().singletonFirst(false));
		A a = injector.bind(I.class).toType(IImpl.class)
				.get(A.class);

		assertThat(a.caller).isNotNull().isEqualTo(InjectorTest.class);
		assertThat(a.b.c().caller).isNotNull().isEqualTo(B.class);
	}
}

class A implements CallerAware {
	final B b;

	A(B b) {
		this.b = b;
	}

	Class<?> caller;

	@Override
	public void setCaller(Class<?> caller) {
		this.caller = caller;
	}
}

record B(C c) {
}

class C implements CallerAware {

	final I i;
	final J j;

	Class<?> caller;

	@Override
	public void setCaller(Class<?> caller) {
		this.caller = caller;
	}

	@Inject
	D d;

	C(I i, J j) {
		this.i = i;
		this.j = j;
	}
}

record D(I i, J j) {

}

interface I {

}

class J implements CallerAware {

	Class<?> caller;

	@Override
	public void setCaller(Class<?> caller) {
		this.caller = caller;
	}
}

class IImpl implements I {
}

