package com.wiredi.processor.tck.domain.override;

import com.wiredi.processor.tck.infrastructure.TckCondition;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

public class OverwrittenTestClass implements TckTestCase {

	protected final TckCondition overwrittenMethodsWillOnlyBeInvokedOnce = TckCondition.shouldNotFail("A method annotated with @Inject that overrides another method annotated with @Inject will only be injected once per injection request per instance");
	protected final TckCondition overwritingMethodPreventsInjection = TckCondition.shouldNotFail("A method with no @Inject annotation that overrides a method annotated with @Inject will not be injected.");
	protected final TckCondition overwritingMethodEnablesInjection = TckCondition.mustSucceed("A method with @Inject annotation that overrides a method annotated with no @Inject will be injected but only in the overwriting method.");
	protected final TckCondition overwrittenMethodDefinesOtherQualifiers = TckCondition.shouldNotFail("If one injectable method overrides another, the overriding method's parameters do not automatically inherit qualifiers from the overridden method's parameters.");
	protected final TckCondition notOverwrittenMethodWithInjectIsInvoked = TckCondition.mustSucceed("A method annotated with @Inject that is not overwritten should be called");
	protected final TckCondition injectWithoutParameterIsInvoked = TckCondition.mustSucceed("A method annotated with @Inject that has no parameter is still invoked");

	@Inject
	public void injectQualifiedValue(@Named("qualified") IDependency dependency) {
		overwrittenMethodsWillOnlyBeInvokedOnce.failure("The overwritten method should not be called");
	}

	@Inject
	public void injectUnqualifiedValue(IDependency dependency) {
		overwrittenMethodsWillOnlyBeInvokedOnce.failure("The overwritten method should not be called");
	}

	@Inject
	public void injectInOverwrittenOnly() {
		overwritingMethodPreventsInjection.failure("The overwritten method was invoked, though the overwriting class shadows the annotation");
		overwrittenMethodsWillOnlyBeInvokedOnce.failure("The overwritten method should not be called");
	}

	public void injectInOverwritingOnly() {
		overwritingMethodEnablesInjection.failure("The overwritten method should not have been called");
		overwrittenMethodsWillOnlyBeInvokedOnce.failure("The overwritten method should not be called");
	}

	@Inject
	public void injectInBoth() {
		overwritingMethodEnablesInjection.failure("The overwritten method should not have been called");
		overwrittenMethodsWillOnlyBeInvokedOnce.failure("The overwritten method should not be called");
	}

	@Inject
	public void notOverwritten() {
		notOverwrittenMethodWithInjectIsInvoked.success();
	}

	@Override
	public Collection<DynamicNode> dynamicTests() {
		return List.of(
				overwritingMethodPreventsInjection.toDynamicTest(),
				overwrittenMethodsWillOnlyBeInvokedOnce.toDynamicTest(),
				overwrittenMethodDefinesOtherQualifiers.toDynamicTest(),
				overwritingMethodEnablesInjection.toDynamicTest(),
				notOverwrittenMethodWithInjectIsInvoked.toDynamicTest(),
				injectWithoutParameterIsInvoked.toDynamicTest()
		);
	}
}
