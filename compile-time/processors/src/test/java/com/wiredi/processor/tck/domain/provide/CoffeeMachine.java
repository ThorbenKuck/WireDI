package com.wiredi.processor.tck.domain.provide;

import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.domain.provide.coffee.*;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import com.wiredi.runtime.WireContainer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class CoffeeMachine implements TckTestCase {

	private final Coffee fruityCoffee;
	private final Arabica arabica;
	private final Coffee earthyCoffee;
	private final Robusta robusta;
	private final Coffee primaryCoffee;
	private final WireContainer wireContainer;

	public CoffeeMachine(
			@Fruity Coffee fruityCoffee,
			Arabica arabica,
			@Earthy Coffee earthyCoffee,
			Robusta robusta,
			Coffee primaryCoffee,
			WireContainer wireContainer
	) {
		this.fruityCoffee = fruityCoffee;
		this.arabica = arabica;
		this.earthyCoffee = earthyCoffee;
		this.robusta = robusta;
 		this.primaryCoffee = primaryCoffee;
		this.wireContainer = wireContainer;
	}

	@Override
	public Collection<DynamicNode> dynamicTests() {
		return List.of(
				dynamicTest("The fruity coffee should be Arabica", () -> assertThat(fruityCoffee).isInstanceOf(Arabica.class)),
				dynamicTest("The earthy coffee should be Robusta", () -> assertThat(earthyCoffee).isInstanceOf(Robusta.class)),
				dynamicTest("The primary coffee should be Robusta", () -> assertThat(primaryCoffee).isInstanceOf(Robusta.class)),
				dynamicTest("The fruity coffee should not be a singleton", () -> assertThat(fruityCoffee).isNotSameAs(arabica)),
				dynamicTest("The earthy coffee should be a singleton", () -> assertThat(earthyCoffee).isSameAs(robusta)),
				dynamicTest("The primary coffee should be robusta coffee", () -> assertThat(primaryCoffee).isSameAs(robusta)),
				dynamicTest("There should be two types of coffee registered", () -> Assertions.assertThat(wireContainer.getAll(Coffee.class)).hasSize(2)),
				dynamicTest("The registered coffee classes should be robusta and arabica", () -> Assertions.assertThat(wireContainer.getAll(Coffee.class).stream().<Class<? extends Coffee>>map(Coffee::getClass).toList()).containsExactlyInAnyOrder(Arabica.class, Robusta.class))
		);
	}
}
