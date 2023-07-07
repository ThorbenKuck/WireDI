package com.wiredi.processor.model;

import jakarta.annotation.PostConstruct;

public class QualifiedCandidate {

	private final WireCandidate3 candidate3;

	public QualifiedCandidate(WireCandidate3 candidate3) {
		this.candidate3 = candidate3;
	}

	@PostConstruct
	public void postConstruct() {

	}

}
