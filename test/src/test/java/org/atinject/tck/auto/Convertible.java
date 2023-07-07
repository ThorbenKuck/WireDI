/*
 * Copyright (C) 2009 The JSR-330 Expert Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atinject.tck.auto;

import com.wiredi.annotations.Wire;
import com.wiredi.test.TestContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import static org.junit.Assert.assertTrue;

@Wire
public class Convertible implements Car {

	@Inject
	static Seat staticFieldPlainSeat;
	@Inject
	@Drivers
	static Seat staticFieldDriversSeat;
	@Inject
	static Tire staticFieldPlainTire;
	@Inject
	@Named("spare")
	static Tire staticFieldSpareTire;
	@Inject
	static Provider<Seat> staticFieldPlainSeatProvider = nullProvider();
	@Inject
	@Drivers
	static Provider<Seat> staticFieldDriversSeatProvider = nullProvider();
	@Inject
	static Provider<Tire> staticFieldPlainTireProvider = nullProvider();
	@Inject
	@Named("spare")
	static Provider<Tire> staticFieldSpareTireProvider = nullProvider();
	private static Seat staticMethodPlainSeat;
	private static Seat staticMethodDriversSeat;
	private static Tire staticMethodPlainTire;
	private static Tire staticMethodSpareTire;
	private static Provider<Seat> staticMethodPlainSeatProvider = nullProvider();
	private static Provider<Seat> staticMethodDriversSeatProvider = nullProvider();
	private static Provider<Tire> staticMethodPlainTireProvider = nullProvider();
	private static Provider<Tire> staticMethodSpareTireProvider = nullProvider();
	@Inject
	@Drivers
	Seat driversSeatA;

	@Inject
	@Drivers
	Seat driversSeatB;

	@Inject
	SpareTire spareTire;

	@Inject
	Cupholder cupholder;

	@Inject
	Provider<Engine> engineProvider;

	@Inject
	Seat fieldPlainSeat;

	@Inject
	@Drivers
	Seat fieldDriversSeat;

	@Inject
	Tire fieldPlainTire;

	@Inject
	@Named("spare")
	Tire fieldSpareTire;

	@Inject
	Provider<Seat> fieldPlainSeatProvider = nullProvider();

	@Inject
	@Drivers
	Provider<Seat> fieldDriversSeatProvider = nullProvider();

	@Inject
	Provider<Tire> fieldPlainTireProvider = nullProvider();

	@Inject
	@Named("spare")
	Provider<Tire> fieldSpareTireProvider = nullProvider();
	private boolean methodWithZeroParamsInjected;
	private boolean methodWithMultipleParamsInjected;
	private boolean methodWithNonVoidReturnInjected;
	private Seat constructorPlainSeat;
	private Seat constructorDriversSeat;
	private Tire constructorPlainTire;
	private Tire constructorSpareTire;
	private Provider<Seat> constructorPlainSeatProvider = nullProvider();
	private Provider<Seat> constructorDriversSeatProvider = nullProvider();
	private Provider<Tire> constructorPlainTireProvider = nullProvider();
	private Provider<Tire> constructorSpareTireProvider = nullProvider();
	private Seat methodPlainSeat;
	private Seat methodDriversSeat;
	private Tire methodPlainTire;
	private Tire methodSpareTire;
	private Provider<Seat> methodPlainSeatProvider = nullProvider();
	private Provider<Seat> methodDriversSeatProvider = nullProvider();
	private Provider<Tire> methodPlainTireProvider = nullProvider();
	private Provider<Tire> methodSpareTireProvider = nullProvider();

	@Inject
	Convertible(
			Seat plainSeat,
			@Drivers Seat driversSeat,
			Tire plainTire,
			@Named("spare") Tire spareTire,
			Provider<Seat> plainSeatProvider,
			@Drivers Provider<Seat> driversSeatProvider,
			Provider<Tire> plainTireProvider,
			@Named("spare") Provider<Tire> spareTireProvider) {
		constructorPlainSeat = plainSeat;
		constructorDriversSeat = driversSeat;
		constructorPlainTire = plainTire;
		constructorSpareTire = spareTire;
		constructorPlainSeatProvider = plainSeatProvider;
		constructorDriversSeatProvider = driversSeatProvider;
		constructorPlainTireProvider = plainTireProvider;
		constructorSpareTireProvider = spareTireProvider;
	}

	Convertible() {
		throw new AssertionError("Unexpected call to non-injectable constructor");
	}

	@Inject
	static void injectStaticMethodWithManyArgs(
			Seat plainSeat,
			@Drivers Seat driversSeat,
			Tire plainTire,
			@Named("spare") Tire spareTire,
			Provider<Seat> plainSeatProvider,
			@Drivers Provider<Seat> driversSeatProvider,
			Provider<Tire> plainTireProvider,
			@Named("spare") Provider<Tire> spareTireProvider) {
		staticMethodPlainSeat = plainSeat;
		staticMethodDriversSeat = driversSeat;
		staticMethodPlainTire = plainTire;
		staticMethodSpareTire = spareTire;
		staticMethodPlainSeatProvider = plainSeatProvider;
		staticMethodDriversSeatProvider = driversSeatProvider;
		staticMethodPlainTireProvider = plainTireProvider;
		staticMethodSpareTireProvider = spareTireProvider;
	}

	public void assertThatDriverSeatsAreWiredCorrectly(TestContext<Seat> context) {
		Class<DriversSeat> type = DriversSeat.class;
		context.theConstructorInjectionPoint("constructorDriversSeat", constructorDriversSeat).isExactlyInstanceOf(type);
		context.theConstructorInjectionPoint("constructorDriversSeatProvider", constructorDriversSeatProvider).isExactlyInstanceOf(type);

		context.theFieldInjectionPoint("driversSeatA", driversSeatA).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("driversSeatB", driversSeatB).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("fieldDriversSeat", fieldDriversSeat).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("fieldDriversSeatProvider", fieldDriversSeatProvider).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldDriversSeat", staticFieldDriversSeat).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldDriversSeatProvider", staticFieldDriversSeatProvider).isExactlyInstanceOf(type);

		context.theMethodInjectionPoint("methodDriversSeat", methodDriversSeat).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("methodDriversSeatProvider", methodDriversSeatProvider).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodDriversSeat", staticMethodDriversSeat).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodDriversSeatProvider", staticMethodDriversSeatProvider).isExactlyInstanceOf(type);

		context.allInjectionPointValuesAreTheSame();
		context.allInjectedProvidersAreEqualBotNotSame();
	}

	public void assertThatPlainSeatsAreWiredCorrectly(TestContext<Seat> context) {
		Class<Seat> type = Seat.class;
		context.theConstructorInjectionPoint("constructorPlainSeat", constructorPlainSeat).isExactlyInstanceOf(type);
		context.theConstructorInjectionPoint("constructorPlainSeatProvider", constructorPlainSeatProvider).isExactlyInstanceOf(type);

		context.theFieldInjectionPoint("fieldPlainSeat", fieldPlainSeat).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("fieldPlainSeatProvider", fieldPlainSeatProvider).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldPlainSeat", staticFieldPlainSeat).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldPlainSeatProvider", staticFieldPlainSeatProvider).isExactlyInstanceOf(type);

		context.theMethodInjectionPoint("methodPlainSeat", methodPlainSeat).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("methodPlainSeatProvider", methodPlainSeatProvider).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodDriversSeat", staticMethodPlainSeat).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodPlainSeatProvider", staticMethodPlainSeatProvider).isExactlyInstanceOf(type);

		context.allInjectionPointValuesAreTheSame();
		context.allInjectedProvidersAreEqualBotNotSame();
	}

	public void assertThatSpareTireIsWiredCorrectly(TestContext<Tire> context) {
		Class<SpareTire> type = SpareTire.class;
		context.theConstructorInjectionPoint("constructorSpareTire", constructorSpareTire).isExactlyInstanceOf(type);
		context.theConstructorInjectionPoint("constructorSpareTireProvider", constructorSpareTireProvider).isExactlyInstanceOf(type);

		context.theFieldInjectionPoint("spareTire", spareTire).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("fieldSpareTire", fieldSpareTire).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldSpareTire", staticFieldSpareTire).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldSpareTireProvider", staticFieldSpareTireProvider).isExactlyInstanceOf(type);

		context.theMethodInjectionPoint("methodSpareTire", methodSpareTire).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("methodSpareTireProvider", methodSpareTireProvider).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodSpareTire", staticMethodSpareTire).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodSpareTireProvider", staticMethodSpareTireProvider).isExactlyInstanceOf(type);

		context.allInjectionPointValuesAreTheSame();
		context.allInjectedProvidersAreEqualBotNotSame();
	}

	public void assertThatPlainTireIsWiredCorrectly(TestContext<Tire> context) {
		Class<Tire> type = Tire.class;
		context.theConstructorInjectionPoint("constructorSpareTire", constructorPlainTire).isExactlyInstanceOf(type);
		context.theConstructorInjectionPoint("constructorSpareTireProvider", constructorPlainTireProvider).isExactlyInstanceOf(type);

		context.theFieldInjectionPoint("fieldSpareTire", fieldPlainTire).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldSpareTire", staticFieldPlainTire).isExactlyInstanceOf(type);
		context.theFieldInjectionPoint("staticFieldSpareTireProvider", staticFieldPlainTireProvider).isExactlyInstanceOf(type);

		context.theMethodInjectionPoint("methodSpareTire", methodPlainTire).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("methodSpareTireProvider", methodPlainTireProvider).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodSpareTire", staticMethodPlainTire).isExactlyInstanceOf(type);
		context.theMethodInjectionPoint("staticMethodSpareTireProvider", staticMethodPlainTireProvider).isExactlyInstanceOf(type);

		context.allInjectionPointValuesAreTheSame();
		context.allInjectedProvidersAreEqualBotNotSame();
	}

	public void assertThatEngineIsWiredCorrectly(TestContext<Engine> context) {
		context.theFieldInjectionPoint("engineProvider", engineProvider).isExactlyInstanceOf(V8Engine.class);
	}

	public void assertThatCupholderIsWiredCorrectly(TestContext<Cupholder> context) {
		context.theFieldInjectionPoint("cupholder", cupholder).isExactlyInstanceOf(Cupholder.class);
	}

	public void asserThatMethodInjectionAreOk() {
		assertTrue("The method with zero parameters was not injected", methodWithZeroParamsInjected);
		assertTrue("The method with multiple parameters was not injected", methodWithMultipleParamsInjected);
		assertTrue("The method with non void return value was not injected", methodWithNonVoidReturnInjected);
	}

	/**
	 * Returns a provider that always returns null. This is used as a default
	 * value to avoid null checks for omitted provider injections.
	 */
	private static <T> Provider<T> nullProvider() {
		return () -> null;
	}

	void setSeat(Seat unused) {
		throw new AssertionError("Unexpected call to non-injectable method");
	}

	@Inject
	void injectMethodWithZeroArgs() {
		methodWithZeroParamsInjected = true;
	}

	@Inject
	String injectMethodWithNonVoidReturn() {
		methodWithNonVoidReturnInjected = true;
		return "unused";
	}

	@Inject
	void injectInstanceMethodWithManyArgs(
			Seat plainSeat,
			@Drivers Seat driversSeat,
			Tire plainTire,
			@Named("spare") Tire spareTire,
			Provider<Seat> plainSeatProvider,
			@Drivers Provider<Seat> driversSeatProvider,
			Provider<Tire> plainTireProvider,
			@Named("spare") Provider<Tire> spareTireProvider) {
		methodWithMultipleParamsInjected = true;

		methodPlainSeat = plainSeat;
		methodDriversSeat = driversSeat;
		methodPlainTire = plainTire;
		methodSpareTire = spareTire;
		methodPlainSeatProvider = plainSeatProvider;
		methodDriversSeatProvider = driversSeatProvider;
		methodPlainTireProvider = plainTireProvider;
		methodSpareTireProvider = spareTireProvider;
	}

}
