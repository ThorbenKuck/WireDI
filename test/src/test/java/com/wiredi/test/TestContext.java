package com.wiredi.test;

import jakarta.inject.Provider;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.SoftAssertions;

import java.util.ArrayList;
import java.util.List;

public class TestContext<T> implements AutoCloseable {

	private final SoftAssertions softAssertions = new SoftAssertions();
	private final List<T> injectedClasses = new ArrayList<>();
	private final List<Provider<T>> injectedProviders = new ArrayList<>();

	public ObjectAssert<T> theFieldInjectionPoint(String name, T object) {
		injectedClasses.add(object);
		return assertThat(object).withFailMessage("[FIELD] The field " + name + " was wired incorrectly");
	}

	public ObjectAssert<T> theMethodInjectionPoint(String name, T object) {
		injectedClasses.add(object);
		return assertThat(object).withFailMessage("[METHOD] The field " + name + " was wired incorrectly");
	}

	public ObjectAssert<T> theConstructorInjectionPoint(String name, T object) {
		injectedClasses.add(object);
		return assertThat(object).withFailMessage("[CONSTRUCTOR] The field " + name + " was wired incorrectly");
	}

	public ObjectAssert<T> theFieldInjectionPoint(String name, Provider<T> object) {
		injectedProviders.add(object);
		injectedClasses.add(object.get());
		return assertThat(object.get()).withFailMessage("[FIELD] The field " + name + " was wired incorrectly");
	}

	public ObjectAssert<T> theMethodInjectionPoint(String name, Provider<T> object) {
		injectedProviders.add(object);
		injectedClasses.add(object.get());
		return assertThat(object.get()).withFailMessage("[METHOD] The field " + name + " was wired incorrectly");
	}

	public ObjectAssert<T> theConstructorInjectionPoint(String name, Provider<T> object) {
		injectedProviders.add(object);
		injectedClasses.add(object.get());
		return assertThat(object.get()).withFailMessage("[CONSTRUCTOR] The field " + name + " was wired incorrectly");
	}

	public void allInjectionPointValuesAreTheSame() {
		ObjectAssert<T> assertThat = softAssertions.assertThat(injectedClasses.remove(0))
				.withFailMessage("All injection points should be the same instance");

		injectedClasses.forEach(assertThat::isSameAs);
		injectedClasses.clear();
	}

	public void allInjectedProvidersAreEqualBotNotSame() {
		ObjectAssert<Provider<T>> equalAssertions = softAssertions.assertThat(injectedProviders.remove(0))
				.withFailMessage("All injected providers are equal");
		injectedProviders.forEach(equalAssertions::isEqualTo);

		ObjectAssert<Provider<T>> notSameAssertion = softAssertions.assertThat(injectedProviders.remove(0))
				.withFailMessage("All injection points are not same");
		injectedProviders.forEach(notSameAssertion::isNotSameAs);
		injectedProviders.clear();
	}

	public ObjectAssert<T> assertThat(T object) {
		return softAssertions.assertThat(object);
	}

	public void assertAll() {
		injectedClasses.clear();
		injectedProviders.clear();
		softAssertions.assertAll();
	}

	@Override
	public void close() throws Exception {
		assertAll();
	}
}
