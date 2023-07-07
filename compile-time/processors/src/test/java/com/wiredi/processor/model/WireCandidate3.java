package com.wiredi.processor.model;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class WireCandidate3 {

	@Inject
	protected WireCandidate1 wireCandidate1;

	@PostConstruct
	protected void setup() {

	}

}
