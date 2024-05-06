package com.wiredi.processor.tck.domain.override;

import com.wiredi.annotations.Wire;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Wire(proxy = false)
public class OverwritingTestClass extends OverwrittenTestClass {

	@Override
	public void injectInOverwrittenOnly() {
		overwritingMethodPreventsInjection.failure("The overwriting function was invoked, even though it shadows the @Inject annotation");
	}

	@Override
	@Inject
	public void injectQualifiedValue(IDependency dependency) {
		if (dependency instanceof QualifiedDependency) {
			overwrittenMethodDefinesOtherQualifiers.failure("The overwriting method removed the qualifier and therefor the parameter should have been unqualified");
		}
	}

	@Override
	@Inject
	public void injectUnqualifiedValue(@Named("qualified") IDependency dependency) {
		if (!(dependency instanceof QualifiedDependency)) {
			overwrittenMethodDefinesOtherQualifiers.failure("The overwriting method add the qualifier and therefor the parameter should have been qualified");
		}
	}

	@Override
	@Inject
	public void injectInOverwritingOnly() {
		overwritingMethodEnablesInjection.success();
	}

	@Inject
	public void injectWithoutParameter() {
		injectWithoutParameterIsInvoked.success();
	}

	@Override
	@Inject
	public void injectInBoth() {
		// This method tests, that the child is not invoked. We do not care here
	}
}
