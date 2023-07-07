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
import com.wiredi.test.TckCondition;
import jakarta.inject.Inject;
import org.atinject.tck.auto.accessories.RoundThing;
import org.atinject.tck.auto.accessories.SpareTire;

@Wire
public class Tire extends RoundThing {

    protected static final FuelTank NEVER_INJECTED = new FuelTank();

    FuelTank constructorInjection = NEVER_INJECTED;
    @Inject FuelTank fieldInjection = NEVER_INJECTED;
    FuelTank methodInjection = NEVER_INJECTED;
    @Inject static FuelTank staticFieldInjection = NEVER_INJECTED;
    static FuelTank staticMethodInjection = NEVER_INJECTED;

    protected final TckCondition superPrivateMethodInjected = TckCondition.mustSucceed("Private method of superclass is injected exactly once");
    protected final TckCondition superPackagePrivateMethodInjected = TckCondition.mustSucceed("Package private method of superclass is injected exactly once");
    protected final TckCondition superProtectedMethodInjected = TckCondition.shouldNotFail("Protected method of superclass should not be injected, if the subclass overwrites it");
    protected final TckCondition superPublicMethodInjected = TckCondition.shouldNotFail("Public method of superclass is injected exactly once");

    protected final TckCondition subPrivateMethodInjected = TckCondition.mustSucceed("Private method of subclass is injected exactly once");
    protected final TckCondition subPackagePrivateMethodInjected = TckCondition.mustSucceed("Package private method of subclass is injected exactly once");
    protected final TckCondition subProtectedMethodInjected = TckCondition.mustSucceed("Protected method of subclass is injected exactly once");
    protected final TckCondition subPublicMethodInjected = TckCondition.mustSucceed("Public method of subclass is injected exactly once");

    protected final TckCondition superPrivateMethodForOverrideInjected = TckCondition.shouldNotFail("Overriding private methods are not injected if they are not annotated in the subclass");
    protected final TckCondition superPackagePrivateMethodForOverrideInjected = TckCondition.shouldNotFail("Overriding package private methods are not injected if they are not annotated in the subclass");
    protected final TckCondition subPrivateMethodForOverrideInjected = TckCondition.mustSucceed("Private methods annotated with @Inject, that are overwritten in the superclass, are still injected");
    protected final TckCondition subPackagePrivateMethodForOverrideInjected = TckCondition.mustSucceed("Package private methods annotated with @Inject, that are overwritten in the superclass, are still injected");
    protected final TckCondition protectedMethodForOverrideInjected = TckCondition.shouldNotFail("Protected methods annotated with @Inject, that are overwritten in the subclass, are not injected in the superclass");
    protected final TckCondition publicMethodForOverrideInjected = TckCondition.shouldNotFail("Public methods annotated with @Inject, that are overwritten in the subclass, are not injected in the superclass");

    protected final TckCondition fieldsInjectedBeforeMethods = TckCondition.shouldNotFail("Fields should be injected before Methods");
    protected final TckCondition subtypeFieldInjectedBeforeSupertypeMethods = TckCondition.shouldNotFail("Methods of supertypes should be injected after fields of subtypes");
    protected final TckCondition subtypeMethodInjectedBeforeSupertypeMethods = TckCondition.shouldNotFail("Methods of supertypes should be injected before methods of the subtype");
    protected final static TckCondition staticMethodInjectedBeforeStaticFields = TckCondition.shouldNotFail("Static fields should be injected before Static Methods");
    protected final static TckCondition subtypeStaticFieldInjectedBeforeSupertypeStaticMethods = TckCondition.shouldNotFail("Static methods of supertypes should be injected after static fields of subtypes");
    protected final static TckCondition subtypeStaticMethodInjectedBeforeSupertypeStaticMethods = TckCondition.shouldNotFail("Static methods of supertypes should be injected before static methods of the subtype");

    @Inject public Tire(FuelTank constructorInjection) {
        this.constructorInjection = constructorInjection;
    }

    @Inject void supertypeMethodInjection(FuelTank methodInjection) {
        if (!hasTireBeenFieldInjected()) {
            fieldsInjectedBeforeMethods.failure("Tire was field injected, before it was method injected");
        }
        if (!hasSpareTireBeenFieldInjected()) {
            subtypeFieldInjectedBeforeSupertypeMethods.failure("SpareTire (subtype) has been field injected before the Tire (supertype)");
        }
        if (hasSpareTireBeenMethodInjected()) {
            subtypeMethodInjectedBeforeSupertypeMethods.failure("SpareTire (subtype) was method injected before Tire (supertype)");
        }
        this.methodInjection = methodInjection;
    }

    @Inject static void supertypeStaticMethodInjection(FuelTank methodInjection) {
        if (!Tire.hasBeenStaticFieldInjected()) {
            staticMethodInjectedBeforeStaticFields.failure("Tire was field injected, before it was method injected");
        }
        if (!SpareTire.hasBeenStaticFieldInjected()) {
            subtypeStaticFieldInjectedBeforeSupertypeStaticMethods.failure("SpareTire (supertype) has been field injected before the Tire (subtype)");
        }
        if (SpareTire.hasBeenStaticMethodInjected()) {
            subtypeStaticMethodInjectedBeforeSupertypeStaticMethods.failure("SpareTire (supertype) was method injected before Tire (subtype)");
        }
        staticMethodInjection = methodInjection;
    }

    @Inject private void injectPrivateMethod() {
        if (superPrivateMethodInjected.isSuccessful()) {
            superPrivateMethodInjected.failure("The private method 'Tire.injectPrivateMethod' was invoked twice");
        }
        superPrivateMethodInjected.success();
    }

    @Inject void injectPackagePrivateMethod() {
        if (superPackagePrivateMethodInjected.isSuccessful()) {
            superPackagePrivateMethodInjected.failure("The package private method 'Tire.injectPackagePrivateMethod' was invoked twice");
        }
        superPackagePrivateMethodInjected.success();
    }

    @Inject protected void injectProtectedMethod() {
        superProtectedMethodInjected.failure("The protected method 'Tire.injectProtectedMethod' should not be invoked");
    }

    @Inject public void injectPublicMethod() {
        superPublicMethodInjected.failure("The public method 'Tire.injectProtectedMethod' should not be invoked");
    }

    @Inject private void injectPrivateMethodForOverride() {
        subPrivateMethodForOverrideInjected.success();
    }

    @Inject void injectPackagePrivateMethodForOverride() {
        subPackagePrivateMethodForOverrideInjected.success();
    }

    @Inject protected void injectProtectedMethodForOverride() {
        protectedMethodForOverrideInjected.failure("Even though the method is annotated with @Inject, it should not be injected, because it is overwritten without @Inject");
    }

    @Inject public void injectPublicMethodForOverride() {
        publicMethodForOverrideInjected.failure("Even though the method is annotated with @Inject, it should not be injected, because it is overwritten without @Inject");;
    }

    protected final boolean hasTireBeenFieldInjected() {
        return fieldInjection != NEVER_INJECTED;
    }

    protected boolean hasSpareTireBeenFieldInjected() {
        return false;
    }

    protected final boolean hasTireBeenMethodInjected() {
        return methodInjection != NEVER_INJECTED;
    }

    protected static boolean hasBeenStaticFieldInjected() {
        return staticFieldInjection != NEVER_INJECTED;
    }

    protected static boolean hasBeenStaticMethodInjected() {
        return staticMethodInjection != NEVER_INJECTED;
    }

    protected boolean hasSpareTireBeenMethodInjected() {
        return false;
    }

    boolean packagePrivateMethod2Injected;

    @Inject void injectPackagePrivateMethod2() {
        packagePrivateMethod2Injected = true;
    }

    public boolean packagePrivateMethod3Injected;

    @Inject void injectPackagePrivateMethod3() {
        packagePrivateMethod3Injected = true;
    }

    public boolean packagePrivateMethod4Injected;

    void injectPackagePrivateMethod4() {
        packagePrivateMethod4Injected = true;
    }
}
