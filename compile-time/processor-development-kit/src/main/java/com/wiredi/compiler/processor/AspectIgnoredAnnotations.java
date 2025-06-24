package com.wiredi.compiler.processor;

import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import com.wiredi.compiler.processor.lang.AnnotationProcessorResourceResolver;
import com.wiredi.runtime.resources.ResolverContext;
import org.slf4j.Logger;import com.wiredi.runtime.Environment;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AspectIgnoredAnnotations {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(AspectIgnoredAnnotations.class);
    private static final String FILE_NAME = "aop-ignored.types";
    private static final String DEFAULT_FILE_NAME = "default.aop-ignored.types";

    private static final List<String> DEFAULT_IGNORED_ANNOTATIONS = List.of(
            "p:java.lang.[a-zA-Z]+",
            "p:[jakrtvx]+.annotation.PostConstruct",
            "p:[jakrtvx]+.annotation.PreDestroy",
            "p:[jakrtvx]+.inject.[a-zA-Z]+"
    );
    private final List<Predicate<String>> ignoredPredicates = new ArrayList<>();
    private final Types types;
    private final Environment environment;
    private final Filer filer;

    public AspectIgnoredAnnotations(Types types, Environment environment, Filer filer) {
        this.types = types;
        this.environment = environment;
        this.filer = filer;
    }

    /**
     * Sets up the ignored annotations, based on the file located under {@link #FILE_NAME}.
     * <p>
     * This file should contain a list annotations. All these listed annotations should not trigger
     * an aspect oriented proxy to proxy the method.
     * <p>
     * The file can contain both fully qualified instance names and regex. The list might look
     * like this:
     * <pre><code>
     * my.package.Annotation
     * p:my.package.*
     * </code></pre>
     * Lines starting with a <pre>p:</pre> should be interpreted as a pattern. These lines must be
     * compilable by the java Pattern class. So the following line:
     * <pre>p:my.package.*</pre>
     * must be compilable by:
     * <pre>Pattern.compile("my.package.*")</pre>
     *
     * <h4>Include other lists</h4>
     * You can include other lists by stating either of the two lines:
     * <pre><code>
     * #include another-file.types
     * #require yet-another-file.types
     * </code></pre>
     * <ul>
     *     <li><b>#include</b> tries to load another file and include it. If it does not exist, it is ignored</li>
     *     <li><b>#require</b> loads the specified file and throws an exception if the file cannot be found</li>
     * </ul>
     */
    @PostConstruct
    protected void postConstruct() {
        ResourceProtocolResolver protocolResolver = environment.resourceLoader().getResolver("compiler");
        logger.debug("Supported protocol resolvers: {}", environment.resourceLoader().supportedProtocols());
        if (protocolResolver == null) {
            logger.debug("Could not find a protocol resolver for the compiler namespace. Using default.");
            protocolResolver = new AnnotationProcessorResourceResolver(filer);
        }
        Resource resource = protocolResolver.resolve(new ResolverContext("compiler", FILE_NAME));
        if (resource.exists()) {
            try {
                setupIgnoredAnnotations(resource);
            } catch (IOException e) {
                loadDefaultIgnoredAnnotations();
                logger.error("Unable to setup ignored annotations for aspects", e);
            }
        } else {
            loadDefaultIgnoredAnnotations();
        }
    }

    public void loadDefaultIgnoredAnnotations() {
        try {
            setupIgnoredAnnotations(DEFAULT_IGNORED_ANNOTATIONS);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void setupIgnoredAnnotations(@NotNull Resource resource) throws IOException {
        if (resource.doesNotExist()) {
            throw new FileNotFoundException("Could not find " + resource.getFilename() + " to load ignored annotations for AOP generation");
        }
        logger.debug(() -> "Trying to load ignored annotations from " + resource.getFilename());
        try (BufferedReader reader = new BufferedReader(resource.openReader())) {
            List<String> lines = reader.lines().toList();
            setupIgnoredAnnotations(lines);
        }
    }

    public void setupIgnoredAnnotations(List<String> lines) throws IOException {
        for (String line : lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("p:")) {
                    line = line.substring("p:".length());
                } else {
                    line = Pattern.quote(line);
                }

                ignoredPredicates.add(Pattern.compile(line).asMatchPredicate());
            } else if (line.startsWith("#include ")) {
                String toInclude = line.replaceFirst(Pattern.quote("#include "), "");
                Resource subResource = environment.loadResource(toInclude, "compiler");
                if (subResource.exists()) {
                    logger.debug(() -> "Including separate file to include ignored annotations: " + subResource.getFilename());
                    setupIgnoredAnnotations(subResource);
                }
            } else if (line.startsWith("#require ")) {
                String toInclude = line.replaceFirst(Pattern.quote("#require "), "");
                Resource subResource = environment.loadResource(toInclude, "compiler");
                if (!subResource.exists()) {
                    throw new IllegalStateException("Could not include the ignored file types, as the file \"" + toInclude + "\" is not available in the instance processors filer");
                }
                logger.debug(() -> "Including separate file to include ignored annotations: " + subResource.getFilename());
                setupIgnoredAnnotations(subResource);
            }

        }
    }

    public void ignoreIf(Pattern pattern) {
        ignoreIf(pattern.asMatchPredicate());
    }

    public void ignoreIf(Predicate<String> predicate) {
        this.ignoredPredicates.add(predicate);
    }

    public boolean isIgnored(TypeMirror typeMirror) {
        Element element = types.asElement(typeMirror);
        return isIgnored(element);
    }

    public boolean isIgnored(Class<? extends Annotation> type) {
        return isIgnored(type.getName());
    }

    public boolean isIgnored(Element element) {
        if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
            return false;
        }

        return isIgnored(((TypeElement) element).getQualifiedName().toString());
    }

    private boolean isIgnored(String name) {
        for (Predicate<String> className : ignoredPredicates) {
            if (className.test(name)) {
                return true;
            }
        }
        return false;
    }
}
