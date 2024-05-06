package com.wiredi.tests;

import org.junit.jupiter.api.extension.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CaptureOutputExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final Map<ExtensionContext, CapturedOutput> capturedOutputs = new HashMap<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        CaptureOutput annotation = context.getElement()
                .map(it -> it.getAnnotation(CaptureOutput.class))
                .or(() -> context.getTestClass().map(it -> it.getAnnotation(CaptureOutput.class)))
                .orElseThrow(() -> new NoSuchElementException("Unable to find CaptureOutput annotation on " + context.getElement()));

        this.capturedOutputs.computeIfAbsent(context, k -> new OutputCollector().capture(annotation.suppressSystemOut(), annotation.suppressSystemErr()));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Optional.ofNullable(this.capturedOutputs.remove(context))
                .ifPresent(CapturedOutput::close);
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        return CapturedOutput.class.isAssignableFrom(parameterContext.getParameter().getType()) && capturedOutputs.containsKey(extensionContext);
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        return capturedOutputs.get(extensionContext);
    }
}
