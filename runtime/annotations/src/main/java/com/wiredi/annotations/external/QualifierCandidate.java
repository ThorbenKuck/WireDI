package com.wiredi.annotations.external;

public @interface QualifierCandidate {

	Class<?> qualifierType();

	String[] fieldValues() default {};

}
