package com.github.thorbenkuck.di.processor.processors;

import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.builder.AspectAwareProxyBuilder;
import com.github.thorbenkuck.di.processor.builder.IdentifiableProviderClassBuilder;
import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;

import javax.lang.model.element.TypeElement;
import java.util.Objects;

public class WireInformationWriter {

	public static void buildAndWriteFor(WireInformation wireInformation) {
		TypeElement typeElement = wireInformation.getSuggestedRoot();
		if (wireInformation.isProxyExpected() && AspectAwareProxyBuilder.willProxyAnything(wireInformation)) {
			if (!AspectAwareProxyBuilder.eligibleForProxy(typeElement)) {
				throw new ProcessingException(typeElement, "This is not eligible for auto proxy.");
			}

			Objects.requireNonNull(wireInformation, "The WireInformation somehow become null");
			new AspectAwareProxyBuilder(wireInformation)
					.addDelegatingConstructors()
					.overwriteMethods()
					.appendWireAnnotations()
					.buildAndWrite("A Proxy instance, wrapping " + wireInformation.getSuggestedRoot().getSimpleName() + " to allow for AOP");
		} else {
			IdentifiableProviderClassBuilder builder = new IdentifiableProviderClassBuilder(wireInformation)
					.overwriteAllRequiredMethods();
			if (wireInformation.getBuilderMethod().isPresent()) {
				builder.identifyingAProviderSource()
						.buildAndWrite("This class is used to identify wired components produced by a method of a maintained wire candidate");
			} else {
				builder.identifyingAWiredSource()
						.buildAndWrite("This class is used to identify wired components");
			}
		}
	}
}
