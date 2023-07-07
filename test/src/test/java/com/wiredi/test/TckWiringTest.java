package com.wiredi.test;

import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.WireRepository;
import jakarta.inject.Named;
import org.atinject.tck.auto.*;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TckWiringTest {

	private final WireRepository wireRepository = WireRepository.open();

	@Test
	public void test() {
		assertThat(wireRepository.get(TypeIdentifier.of(Seat.class)).getClass()).isEqualTo(Seat.class);
		assertThat(wireRepository.get(TypeIdentifier.of(Seat.class), QualifierType.just(Drivers.class)).getClass()).isEqualTo(DriversSeat.class);
	}

	@Test
	public void assertThatTheCupholderIsWiredCorrectly() {
		wireRepository.get(Cupholder.class).seatMatches(it -> it.getClass().equals(Seat.class));
	}

	@Test
	public void fuelTankValidation() {
		assertThat(wireRepository.tryGet(FuelTank.class)).withFailMessage("FuelTank is not wired").isPresent();
		assertThat(wireRepository.get(FuelTank.class)).withFailMessage("FuelTank is not wired as a singleton").isSameAs(wireRepository.get(FuelTank.class));
	}

	@Test
	public void seatbeltValidation() {
		assertThat(wireRepository.tryGet(Seatbelt.class)).withFailMessage("Seatbelt is not wired").isPresent();
		assertThat(wireRepository.get(Seatbelt.class)).withFailMessage("Seatbelt is not wired as a singleton").isSameAs(wireRepository.get(Seatbelt.class));
	}

	@TestFactory
	public Collection<DynamicTest> v8EngineTest() {
		V8Engine v8Engine = wireRepository.get(V8Engine.class);

		List<DynamicTest> tests = new ArrayList<>(v8Engine.dynamicTests());
		tests.add(dynamicTest("The Engine should be the same as the V8Engine", () -> assertThat(v8Engine).isSameAs(wireRepository.get(Engine.class))));
		tests.add(dynamicTest("The GasEngine should be the same as the V8Engine", () -> assertThat(v8Engine).isSameAs(wireRepository.get(GasEngine.class))));
		return tests;
	}

	@TestFactory
	public Collection<DynamicTest> spareTireTests() {
		SpareTire spareTire = wireRepository.get(SpareTire.class);

		List<DynamicTest> tests = new ArrayList<>(spareTire.dynamicTests());
		tests.add(dynamicTest("The SpareTire should not be the same as the Tire", () -> assertThat(spareTire).isNotSameAs(wireRepository.get(Tire.class))));
		tests.add(dynamicTest("The SpareTire should be the same as the Tire if requested with Qualifier", () -> assertThat(spareTire).isSameAs(wireRepository.get(TypeIdentifier.of(Tire.class), QualifierType.newInstance(Named.class)
						.add("value", "spare")
				.build()))));
		tests.add(dynamicTest("The SpareTire should be the same as the RoundThing", () -> assertThat(spareTire).isSameAs(wireRepository.get(SpareTire.class))));
		return tests;

	}

	@TestFactory
	public Collection<DynamicTest> driversSeatValidation() {
		DriversSeat driversSeat = wireRepository.get(DriversSeat.class);

		return List.of(
				dynamicTest("CupHolder is wired correctly", () -> assertThat(driversSeat.getCupholder()).isNotNull()),
				dynamicTest("CupHolder is singleton", () -> assertThat(driversSeat.getCupholder()).isSameAs(wireRepository.get(Cupholder.class))),
				dynamicTest("The Seat is by default not a DriversSeat", () -> assertThat(driversSeat).isNotSameAs(wireRepository.get(Seat.class))),
				dynamicTest("If the Seat is requested with the qualifier @Drivers, a singleton DriversSeat is found", () -> assertThat(driversSeat).isSameAs(wireRepository.get(TypeIdentifier.of(Seat.class), QualifierType.just(Drivers.class))))
		);
	}

	@TestFactory
	public Collection<DynamicTest> carValidation() {
		Car car = wireRepository.get(Car.class);
		Convertible convertible = wireRepository.get(Convertible.class);

		return List.of(
				dynamicTest("Car is Convertible", () -> assertThat(car)
						.isExactlyInstanceOf(Convertible.class)
						.isSameAs(convertible)),
				dynamicTest("Driver Seat is correctly wired into the convertible", () -> {
					try (TestContext<Seat> testContext = new TestContext<>()) {
						convertible.assertThatDriverSeatsAreWiredCorrectly(testContext);
					}
				}),
				dynamicTest("Plain Seat is correctly wired into the convertible", () -> {
					try (TestContext<Seat> testContext = new TestContext<>()) {
						convertible.assertThatPlainSeatsAreWiredCorrectly(testContext);
					}
				}),
				dynamicTest("Spare Tire is correctly wired into the convertible", () -> {
					try (TestContext<Tire> testContext = new TestContext<>()) {
						convertible.assertThatSpareTireIsWiredCorrectly(testContext);
					}
				}),
				dynamicTest("Plain Tire is correctly wired into the convertible", () -> {
					try (TestContext<Tire> testContext = new TestContext<>()) {
						convertible.assertThatPlainTireIsWiredCorrectly(testContext);
					}
				}),
				dynamicTest("Engine is correctly wired into the convertible", () -> {
					try (TestContext<Engine> testContext = new TestContext<>()) {
						convertible.assertThatEngineIsWiredCorrectly(testContext);
					}
				}),
				dynamicTest("Cupholder is correctly wired into the convertible", () -> {
					try (TestContext<Cupholder> testContext = new TestContext<>()) {
						convertible.assertThatCupholderIsWiredCorrectly(testContext);
					}
				}),
				dynamicTest("The methods where correctly called", convertible::asserThatMethodInjectionAreOk)
		);
	}
}
