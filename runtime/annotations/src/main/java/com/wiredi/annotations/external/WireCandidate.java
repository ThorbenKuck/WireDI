package com.wiredi.annotations.external;

import com.wiredi.annotations.Wire;

public @interface WireCandidate {
	Class<?> value();

	Wire wire() default @Wire;
}
