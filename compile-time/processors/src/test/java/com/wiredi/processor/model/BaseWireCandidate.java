package com.wiredi.processor.model;

import jakarta.inject.Inject;

public class BaseWireCandidate {

	@Inject public WireCandidate2 inheritedPublicWireCandidate2;
	@Inject WireCandidate2 inheritedPackagePrivateWireCandidate2;
	@Inject private WireCandidate2 inheritedPrivateWireCandidate2;

	@Inject public void inheritedPublicWireCandidate2Function(WireCandidate2 candidate2) {}
	@Inject void inheritedPackagePrivateWireCandidate2Function(WireCandidate2 candidate2) {}
	@Inject private void inheritedPrivateWireCandidate2Function(WireCandidate2 candidate2) {}

	@Inject public void inheritedPublicWireCandidate2FunctionWithTwoParameters(WireCandidate2 candidate21, WireCandidate2 candidate22) {}
	@Inject void inheritedPackagePrivateWireCandidate2FunctionWithTwoParameters(WireCandidate2 candidate21, WireCandidate2 candidate22) {}
	@Inject private void inheritedPrivateWireCandidate2FunctionWithTwoParameters(WireCandidate2 candidate21, WireCandidate2 candidate22) {}


}
