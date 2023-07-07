package com.wiredi.processor.model;

import com.wiredi.annotations.Constructed;
import com.wiredi.annotations.Wire;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Wire
public class WireCandidate1 extends BaseWireCandidate {

	@Inject public WireCandidate2 publicWireCandidate2;
	@Inject WireCandidate2 packagePrivateWireCandidate2;
	@Inject private WireCandidate2 privateWireCandidate2;
	private final WireCandidate2 constructorParameter;

	public WireCandidate1(WireCandidate2 constructorParameter) {
		this.constructorParameter = constructorParameter;
	}

	@Inject public void publicWireCandidate2Function(WireCandidate2 candidate2) {}
	@Inject void packagePrivateWireCandidate2Function(WireCandidate2 candidate2) {}
	@Inject private void privateWireCandidate2Function(WireCandidate2 candidate2) {}

	@Inject public void publicWireCandidate2FunctionWithTwoParameters(WireCandidate2 candidate21, WireCandidate2 candidate22) {}
	@Inject void packagePrivateWireCandidate2FunctionWithTwoParameters(WireCandidate2 candidate21, WireCandidate2 candidate22) {}
	@Inject private void privateWireCandidate2FunctionWithTwoParameters(WireCandidate2 candidate21, WireCandidate2 candidate22) {}

	@PostConstruct
	public void postConstruct() {

	}

	@Constructed
	public void constructed(WireCandidate2 wireCandidate21, WireCandidate2 wireCandidate22) {

	}

}
