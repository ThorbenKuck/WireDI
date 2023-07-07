package com.wiredi.processor.model;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;

@Wire
public class WireCandidate2 {

	@Provider
	public WireCandidate3 wireCandidate3() {
		return new WireCandidate3();
	}
}
