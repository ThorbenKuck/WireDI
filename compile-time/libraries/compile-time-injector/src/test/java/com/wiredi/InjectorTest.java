package com.wiredi;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InjectorTest {

	@Test
	public void contractTest() {
		IImpl i = new IImpl();
		Injector injector = new Injector();
		A a = injector
				.bind(I.class).to(i)
				.get(A.class);

		assertThat(a).isNotNull();
		assertThat(a.b).isNotNull();
		assertThat(a.b.c()).isNotNull();
		assertThat(a.b.c().d).isNotNull();
		assertThat(a.b.c().d.i()).isNotNull().isSameAs(i);
	}

}

class A {
	final B b;

	A(B b) {
		this.b = b;
	}
}

record B(C c) {
}

class C {
	@Inject
	D d;
}

record D(I i) {

}

interface I {

}

class IImpl implements I {
}
