package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.WiredTypes;
import com.github.thorbenkuck.di.test.inner.SuperDi;

public class Main {

	public static void main(String[] args) {
		WiredTypes wiredTypes = new WiredTypes();
		wiredTypes.load();
		SuperDi instance = wiredTypes.getInstance(SuperDi.class);
		instance.foo();
	}

}
