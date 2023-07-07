/*
 * Copyright (C) 2009 The JSR-330 Expert Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atinject.tck.auto;

import com.wiredi.test.TckCondition;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.DynamicTest;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class Engine {

    protected TckCondition injectPackagePrivateMethodCondition = TckCondition.mustSucceed("A method annotated with @Inject that overrides another method annotated with @Inject will only be injected once per injection request per instance");
    protected TckCondition publicNoArgsConstructorInjected = TckCondition.mustSucceed("The public NoArgs Constructor should have been called");
    protected TckCondition superPackagePrivateMethodForOverrideInjected = TckCondition.shouldNotFail("A method with no @Inject annotation that overrides a method annotated with @Inject will not be injected.");
    protected TckCondition qualifiersInheritedFromOverriddenMethod = TckCondition.shouldNotFail("If one injectable method overrides another, the overriding method's parameters do not automatically inherit qualifiers from the overridden method's parameters. Injectable Values");
    protected TckCondition subPackagePrivateMethodForOverrideInjected = TckCondition.shouldNotFail("subPackagePrivateMethodForOverrideInjected");
    protected TckCondition overriddenTwiceWithOmissionInMiddleInjected = TckCondition.mustSucceed("overriddenTwiceWithOmissionInMiddleInjected");
    protected TckCondition overriddenTwiceWithOmissionInSubclassInjected = TckCondition.shouldNotFail("overriddenTwiceWithOmissionInSubclassInjected");
    protected TckCondition injectNotOverwrittenMethod = TckCondition.mustSucceed("A method, annotated with @Inject, that is not overwritten, is still invoked");

    public Collection<DynamicTest> dynamicTests() {
        return List.of(
                injectPackagePrivateMethodCondition.toDynamicTest(),
                publicNoArgsConstructorInjected.toDynamicTest(),
                superPackagePrivateMethodForOverrideInjected.toDynamicTest(),
                qualifiersInheritedFromOverriddenMethod.toDynamicTest(),
                subPackagePrivateMethodForOverrideInjected.toDynamicTest(),
                overriddenTwiceWithOmissionInMiddleInjected.toDynamicTest(),
                overriddenTwiceWithOmissionInSubclassInjected.toDynamicTest(),
                injectNotOverwrittenMethod.toDynamicTest()
        );
    }

    @Inject
    public void injectNotOverwritten() {
        if (injectNotOverwrittenMethod.isSuccessful()) {
            injectNotOverwrittenMethod.failure("The method injectNotOverwritten should be invoked only once");
        }
        injectNotOverwrittenMethod.success();
    }

    @Inject
    public void injectQualifiers(
            @Drivers Seat seatA,
            Seat seatB,
            @Named("spare") Tire tireA,
            Tire tireB
    ) {
        qualifiersInheritedFromOverriddenMethod.failure("The overwritten method should not have been invoked");
    }

    @Inject void injectPackagePrivateMethod() {
        injectPackagePrivateMethodCondition.failure("The method was overwritten, hence it should not have been invoked.");
    }

    @Inject void injectPackagePrivateMethodForOverride() {
        superPackagePrivateMethodForOverrideInjected.failure("The Super method was invoked, but it should not have been invoked");
    }

    @Inject public void injectTwiceOverriddenWithOmissionInMiddle() {
        overriddenTwiceWithOmissionInMiddleInjected.failure("The overwritten method should not have been invoked");
    }

    @Inject public void injectTwiceOverriddenWithOmissionInSubclass() {
        overriddenTwiceWithOmissionInSubclassInjected.failure("This Engine class is abstract. Since its parent does not declare @Inject, this method should not be invoked!");
    }
}
