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

import com.wiredi.annotations.Wire;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.atinject.tck.auto.accessories.SpareTire;

@Wire
public class V8Engine extends GasEngine {

    public V8Engine() {
        publicNoArgsConstructorInjected.success();
    }

    @Inject
    @Override
    void injectPackagePrivateMethod() {
        if (injectPackagePrivateMethodCondition.isSuccessful()) {
            injectPackagePrivateMethodCondition.failure("The package private method was invoked twice. It should have been only invoked once.");
        }
        injectPackagePrivateMethodCondition.success();
    }

    void injectPackagePrivateMethodForOverride() {
        subPackagePrivateMethodForOverrideInjected.failure("The method V8Engine.injectPackagePrivateMethodForOverride does not declare @Inject, hence it should not be called");
    }

    /**
     * Qualifiers are swapped from how they appear in the superclass.
     */
    @Inject
    public void injectQualifiers(
            Seat seatA,
            @Drivers Seat seatB,
            Tire tireA,
            @Named("spare") Tire tireB
    ) {
        if (!qualifiersInheritedFromOverriddenMethod.isSuccessful()) {
            qualifiersInheritedFromOverriddenMethod.failure("The injectQualifiers method should not have been called more then once, but only once.");
            return;
        }

        if (seatA instanceof DriversSeat) {
            qualifiersInheritedFromOverriddenMethod.failure("The first seat should have been a plain seat, not a drivers seat as defined in the overwritten method");
        }
        if (!(seatB instanceof DriversSeat)) {
            qualifiersInheritedFromOverriddenMethod.failure("The second seat should have been a drivers seat, not a plain seat as defined in the overwritten method");
        }
        if (tireA instanceof SpareTire) {
            qualifiersInheritedFromOverriddenMethod.failure("The first tire should have been a plain tire, not a SpareTire as defined in the overwritten method");
        }
        if (!(tireB instanceof SpareTire)) {
            qualifiersInheritedFromOverriddenMethod.failure("The first tire should have been a SpareTire, not a plain tire as defined in the overwritten method");
        }
    }

    @Inject
    public void injectTwiceOverriddenWithOmissionInMiddle() {
        overriddenTwiceWithOmissionInMiddleInjected.success();
    }

    public void injectTwiceOverriddenWithOmissionInSubclass() {
        overriddenTwiceWithOmissionInSubclassInjected.failure("The method V8Engine.injectTwiceOverriddenWithOmissionInSubclass does not declare @Inject, hence it should not be called");
    }
}
