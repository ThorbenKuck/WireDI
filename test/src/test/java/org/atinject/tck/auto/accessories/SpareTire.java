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

package org.atinject.tck.auto.accessories;

import com.wiredi.annotations.Wire;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Tire;
import org.junit.jupiter.api.DynamicTest;

import java.util.Collection;
import java.util.List;

@Wire
@Named("spare")
public class SpareTire extends Tire {

    FuelTank constructorInjection = NEVER_INJECTED;
    @Inject FuelTank fieldInjection = NEVER_INJECTED;
    FuelTank methodInjection = NEVER_INJECTED;
    @Inject static FuelTank staticFieldInjection = NEVER_INJECTED;
    static FuelTank staticMethodInjection = NEVER_INJECTED;

    @Inject public SpareTire(FuelTank forSupertype, FuelTank forSubtype) {
        super(forSupertype);
        this.constructorInjection = forSubtype;
    }

    @Inject void subtypeMethodInjection(FuelTank methodInjection) {
        if (!hasSpareTireBeenFieldInjected()) {
            fieldsInjectedBeforeMethods.failure("SpareTire was field injected, before it was method injected");
        }
        this.methodInjection = methodInjection;
    }

    @Inject static void subtypeStaticMethodInjection(FuelTank methodInjection) {
        if (!hasBeenStaticFieldInjected()) {
            staticMethodInjectedBeforeStaticFields.failure("SpareTire was field injected, before it was method injected");
        }
        staticMethodInjection = methodInjection;
    }

    @Inject private void injectPrivateMethod() {
        if (subPrivateMethodInjected.isSuccessful()) {
            subPrivateMethodInjected.failure("The private method 'SpareTire.injectPrivateMethod' was invoked twice");
        }
        subPrivateMethodInjected.success();
    }

    @Inject void injectPackagePrivateMethod() {
        if (subPackagePrivateMethodInjected.isSuccessful()) {
            subPackagePrivateMethodInjected.failure("The package private method 'SpareTire.injectPackagePrivateMethod' was invoked twice");
        }
        subPackagePrivateMethodInjected.success();
    }

    @Inject protected void injectProtectedMethod() {
        if (subProtectedMethodInjected.isSuccessful()) {
            subProtectedMethodInjected.failure("The protected method 'SpareTire.injectProtectedMethod' was invoked twice");
        }
        subProtectedMethodInjected.success();
    }

    @Inject public void injectPublicMethod() {
        if (subPublicMethodInjected.isSuccessful()) {
            subPublicMethodInjected.failure("The public method 'SpareTire.injectPublicMethod' was invoked twice");
        }
        subPublicMethodInjected.success();
    }

    private void injectPrivateMethodForOverride() {
        superPrivateMethodForOverrideInjected.failure("The method 'injectPrivateMethodForOverride' is not annotated with @Inject and should not be injected");
    }

    void injectPackagePrivateMethodForOverride() {
        superPackagePrivateMethodForOverrideInjected.failure("The method 'injectPackagePrivateMethodForOverride' is not annotated with @Inject and should not be injected");
    }

    protected void injectProtectedMethodForOverride() {
        protectedMethodForOverrideInjected.failure("The method 'injectProtectedMethodForOverride' is not annotated with @Inject and should not be injected");
    }

    public void injectPublicMethodForOverride() {
        publicMethodForOverrideInjected.failure("The method 'injectPublicMethodForOverride' is not annotated with @Inject and should not be injected");
    }

    public boolean hasSpareTireBeenFieldInjected() {
        return fieldInjection != NEVER_INJECTED;
    }

    public boolean hasSpareTireBeenMethodInjected() {
        return methodInjection != NEVER_INJECTED;
    }

    public static boolean hasBeenStaticFieldInjected() {
        return staticFieldInjection != NEVER_INJECTED;
    }

    public static boolean hasBeenStaticMethodInjected() {
        return staticMethodInjection != NEVER_INJECTED;
    }

    public boolean packagePrivateMethod2Injected;

    @Inject void injectPackagePrivateMethod2() {
        packagePrivateMethod2Injected = true;
    }

    public boolean packagePrivateMethod3Injected;

    void injectPackagePrivateMethod3() {
        packagePrivateMethod3Injected = true;
    }

    public Collection<DynamicTest> dynamicTests() {
        return List.of(
                superPrivateMethodInjected.toDynamicTest(),
                superPackagePrivateMethodInjected.toDynamicTest(),
                superProtectedMethodInjected.toDynamicTest(),
                superPublicMethodInjected.toDynamicTest(),

                subPrivateMethodInjected.toDynamicTest(),
                subPackagePrivateMethodInjected.toDynamicTest(),
                subProtectedMethodInjected.toDynamicTest(),
                subPublicMethodInjected.toDynamicTest(),

                superPrivateMethodForOverrideInjected.toDynamicTest(),
                superPackagePrivateMethodForOverrideInjected.toDynamicTest(),
                subPrivateMethodForOverrideInjected.toDynamicTest(),
                subPackagePrivateMethodForOverrideInjected.toDynamicTest(),
                protectedMethodForOverrideInjected.toDynamicTest(),
                publicMethodForOverrideInjected.toDynamicTest(),

                fieldsInjectedBeforeMethods.toDynamicTest(),
                subtypeFieldInjectedBeforeSupertypeMethods.toDynamicTest(),
                subtypeMethodInjectedBeforeSupertypeMethods.toDynamicTest(),

                staticMethodInjectedBeforeStaticFields.toDynamicTest(),
                subtypeStaticFieldInjectedBeforeSupertypeStaticMethods.toDynamicTest(),
                subtypeStaticMethodInjectedBeforeSupertypeStaticMethods.toDynamicTest()
        );
    }
}
