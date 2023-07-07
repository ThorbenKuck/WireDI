package com.wiredi.processor.tck.domain;

import com.wiredi.annotations.Wire;
import com.wiredi.processor.tck.infrastructure.TckCondition;
import com.wiredi.processor.tck.infrastructure.TckTestCase;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DynamicNode;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@Wire
public class InjectionTest implements TckTestCase {

	private final TckCondition fieldsAreInjectedBeforeMethods = TckCondition.shouldNotFail("Field Injection happens before the field injection");
	private final TckCondition injectionsAreSingleton = TckCondition.shouldNotFail("All injections are based on singleton instances");

	@Inject
	public Dependency publicDependency;

	@Inject
	protected Dependency protectedDependency;

	@Inject
	Dependency packagePrivateDependency;

	@Inject
	private Dependency privateDependency;

	@Inject
	public void setPublicDependency(Dependency dependency) {
		String source = "public method";
		if (publicDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The public dependency should have been injected before the " + source);
		} else if (publicDependency != dependency) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}

		if (protectedDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The protected dependency should have been injected before the " + source);
		} else if (protectedDependency != dependency) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}

		if (packagePrivateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The package private dependency should have been injected before the " + source);
		} else if (packagePrivateDependency != dependency) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}

		if (privateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The private dependency should have been injected before the " + source);
		} else if (privateDependency != dependency) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Inject
	void setPackagePrivateDependency(Dependency dependency) {
		String source = "package private method";
		if (publicDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The public dependency should have been injected before the " + source);
		} else if (publicDependency != dependency) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}

		if (protectedDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The protected dependency should have been injected before the " + source);
		} else if (protectedDependency != dependency) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}

		if (packagePrivateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The package private dependency should have been injected before the " + source);
		} else if (packagePrivateDependency != dependency) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}

		if (privateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The private dependency should have been injected before the " + source);
		} else if (privateDependency != dependency) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Inject
	protected void setProtectedDependency(Dependency dependency) {
		String source = "protected method";
		if (publicDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The public dependency should have been injected before the " + source);
		} else if (publicDependency != dependency) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}

		if (protectedDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The protected dependency should have been injected before the " + source);
		} else if (protectedDependency != dependency) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}

		if (packagePrivateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The package private dependency should have been injected before the " + source);
		} else if (packagePrivateDependency != dependency) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}

		if (privateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The private dependency should have been injected before the " + source);
		} else if (privateDependency != dependency) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Inject
	private void setPrivateDependency(Dependency dependency) {
		String source = "private method";
		if (publicDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The public dependency should have been injected before the " + source);
		} else if (publicDependency != dependency) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}

		if (protectedDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The protected dependency should have been injected before the " + source);
		} else if (protectedDependency != dependency) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}

		if (packagePrivateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The package private dependency should have been injected before the " + source);
		} else if (packagePrivateDependency != dependency) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}

		if (privateDependency == null) {
			fieldsAreInjectedBeforeMethods.failure("The private dependency should have been injected before the " + source);
		} else if (privateDependency != dependency) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Inject
	public void publicMethodTwoParameters(Dependency dependency1, Dependency dependency2) {
		String source = "public method";
		if (dependency1 != dependency2) {
			injectionsAreSingleton.failure("The dependencies injected are not the same in the " + source);
		}
		if (publicDependency != dependency1) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}
		if (packagePrivateDependency != dependency1) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}
		if (protectedDependency != dependency1) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}
		if (privateDependency != dependency1) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Inject
	void packagePrivateMethodTwoParameters(Dependency dependency1, Dependency dependency2) {
		String source = "package private method";
		if (dependency1 != dependency2) {
			injectionsAreSingleton.failure("The dependencies injected are not the same in the " + source);
		}
		if (publicDependency != dependency1) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}
		if (packagePrivateDependency != dependency1) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}
		if (protectedDependency != dependency1) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}
		if (privateDependency != dependency1) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Inject
	protected void protectedMethodTwoParameters(Dependency dependency1, Dependency dependency2) {
		String source = "protected method";
		if (dependency1 != dependency2) {
			injectionsAreSingleton.failure("The dependencies injected are not the same in the " + source);
		}
		if (publicDependency != dependency1) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}
		if (packagePrivateDependency != dependency1) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}
		if (protectedDependency != dependency1) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}
		if (privateDependency != dependency1) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Inject
	private void privateMethodTwoParameters(Dependency dependency1, Dependency dependency2) {
		String source = "private method";
		if (dependency1 != dependency2) {
			injectionsAreSingleton.failure("The dependencies injected are not the same in the " + source);
		}
		if (publicDependency != dependency1) {
			injectionsAreSingleton.failure("The public field was not the same as injected into the " + source);
		}
		if (packagePrivateDependency != dependency1) {
			injectionsAreSingleton.failure("The package private field was not the same as injected into the " + source);
		}
		if (protectedDependency != dependency1) {
			injectionsAreSingleton.failure("The protected field was not the same as injected into the " + source);
		}
		if (privateDependency != dependency1) {
			injectionsAreSingleton.failure("The private field was not the same as injected into the " + source);
		}
	}

	@Override
	public Collection<DynamicNode> dynamicTests() {
		return List.of(
				dynamicTest("Public field is injected successfully", () -> assertThat(publicDependency).isNotNull()),
				dynamicTest("Package Private field is injected successfully", () -> assertThat(packagePrivateDependency).isNotNull()),
				dynamicTest("Protected field is injected successfully", () -> assertThat(protectedDependency).isNotNull()),
				dynamicTest("Private field is injected successfully", () -> assertThat(privateDependency).isNotNull()),
				fieldsAreInjectedBeforeMethods.toDynamicTest(),
				injectionsAreSingleton.toDynamicTest()
		);
	}
}
