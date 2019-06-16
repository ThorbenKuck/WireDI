package com.github.thorbenkuck.di;

public class DiProperties {

	public static boolean doAutoLoad() {
		String autoLoad = System.getProperty("di.simple.autoload");
		if(autoLoad == null) {
			autoLoad = "true";
		}

		return Boolean.parseBoolean(autoLoad);
	}

}
