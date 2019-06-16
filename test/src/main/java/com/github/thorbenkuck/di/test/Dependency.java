package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.Wire;

@Wire(to = IDependency.class)
class Dependency implements IDependency {

	{
		System.out.println("Dependency Instantiated");
	}

}
