package com.wiredi.lang;

public class Booleans {

	public static boolean parseStrict(String string) {
		if (string == null) {
			throw new IllegalArgumentException("Null cannot be parsed as boolean");
		}
		String lowerCase = string.toLowerCase();
		switch (lowerCase) {
			case "1", "true" -> { return true; }
			case "0", "false" -> { return false; }
			default -> throw new IllegalArgumentException("The value " + string + " cannot be parsed as a boolean");
		}
	}
}
