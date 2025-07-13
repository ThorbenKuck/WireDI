package com.wiredi.processor.tck.domain.provide.coffee;

import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.scopes.Prototype;
import com.wiredi.annotations.stereotypes.Configuration;

@Configuration
public class CoffeeConfiguration {

	@Fruity
	@Provider
	@Prototype
	public Arabica arabica() {
		return new Arabica();
	}

	@Earthy
	@Primary
	@Provider
	public Robusta robusta() {
		return new Robusta();
	}
}
