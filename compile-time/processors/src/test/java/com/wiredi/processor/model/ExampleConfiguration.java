package com.wiredi.processor.model;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;
import com.wiredi.annotations.stereotypes.Configuration;
import jakarta.inject.Named;

@Wire
public class ExampleConfiguration {

	@Provider
	@Named("Foo")
	public QualifiedCandidate namedQualification(WireCandidate3 wireCandidate3) {
		return new QualifiedCandidate(wireCandidate3);
	}

	@Provider
	@TestQualifier(1)
	public QualifiedCandidate testQualifierCandidate(WireCandidate3 wireCandidate3) {
		return new QualifiedCandidate(wireCandidate3);
	}
}
