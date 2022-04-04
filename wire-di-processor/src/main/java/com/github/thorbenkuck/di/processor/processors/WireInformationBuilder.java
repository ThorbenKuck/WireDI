package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.AspectAwareProxyBuilder;
import com.github.thorbenkuck.di.processor.builder.IdentifiableProviderClassBuilder;
import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;

import javax.lang.model.element.TypeElement;
import java.util.Objects;

public class WireInformationBuilder {

    public static void buildFor(WireInformation wireInformation) {
        TypeElement typeElement = wireInformation.getWireCandidate();
        if(wireInformation.isProxyExpected() && AspectAwareProxyBuilder.willProxyAnything(wireInformation)) {
            if(!AspectAwareProxyBuilder.eligibleForProxy(typeElement)) {
                throw new ProcessingException(typeElement, "This is not eligible for auto proxy.");
            }

            Objects.requireNonNull(wireInformation, "The WireInformation somehow become null");
            new AspectAwareProxyBuilder(wireInformation)
                    .addDelegatingConstructors()
                    .overwriteMethods()
                    .appendWireAnnotations()
                    .buildAndWrite("A Proxy instance, wrapping " + wireInformation.getWireCandidate().getSimpleName() + " to allow for AOP");
        } else {
            new IdentifiableProviderClassBuilder(wireInformation)
                    .overwriteAllRequiredMethods()
                    .identifyingAWiredSource()
                    .buildAndWrite("This class is used to identify wired components");
        }
    }
}
