package com.wiredi.compiler.tests.junit;

import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.compiler.tests.Compiler;
import com.wiredi.compiler.tests.Diagnostics;
import com.wiredi.compiler.tests.files.FileManagerState;
import com.wiredi.compiler.tests.files.utils.FileObjectFactory;
import com.wiredi.compiler.tests.files.utils.JavaFileObjectFactory;
import com.wiredi.compiler.tests.files.utils.ResourceFileObjectFactory;
import com.wiredi.compiler.tests.result.Compilation;
import com.wiredi.runtime.lang.ReflectionsHelper;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.PreconditionViolationException;

import javax.annotation.processing.Processor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class CompilerTestExtension implements ParameterResolver, AfterEachCallback {

    private static final Map<ExtensionContext, CompilerState> cache = new HashMap<>();
    private static final TypeMap<Function<CompilerState, ?>> typeFactories = new TypeMap<>();

    static {
        typeFactories.put(Compiler.class, it -> it.compiler);
        typeFactories.put(Compilation.class, CompilerState::getCompilation);
        typeFactories.put(FileManagerState.class, it -> it.compiler.fileManagerState());
        typeFactories.put(Diagnostics.class, it -> it.getCompilation().diagnostics());
        typeFactories.put(FileObjectFactory.class, it -> it.compiler.fileObjectFactory());
        typeFactories.put(JavaFileObjectFactory.class, it -> it.compiler.fileObjectFactory().javaFileObjectFactory());
        typeFactories.put(ResourceFileObjectFactory.class, it -> it.compiler.fileObjectFactory().resourceFileObjectFactory());
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        cache.remove(extensionContext);
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        return typeFactories.containsKey(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        CompilerState state = cache.computeIfAbsent(extensionContext, (it) -> {
            Compiler instance = Compiler.javac();
            setupCompiler(extensionContext, instance);
            return new CompilerState(instance);
        });

        return Optional.ofNullable(typeFactories.get(parameterContext.getParameter().getType()).apply(state))
                .orElseThrow(() -> new PreconditionViolationException("Could not resolve the parameter of type" + parameterContext.getParameter().getType()));
    }

    private void setupCompiler(ExtensionContext extensionContext, Compiler compiler) {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        CompilerSetup rootAnnotation = testClass.getAnnotation(CompilerSetup.class);
        compiler.setRootFolder(rootAnnotation.rootFolder());

        // Step one: Configurations on package
        Optional.ofNullable(testClass.getPackage())
                .map(it -> it.getAnnotationsByType(CompilerTest.class))
                .ifPresent(list -> Arrays.stream(list).forEach(it -> register(compiler, it.classes(), it.classesIn(), it.options(), it.processors())));
        // Step two: Configurations on inherited classes
        ReflectionsHelper.getAllInheritedClasses(testClass).stream()
                .flatMap(it -> Arrays.stream(it.getAnnotationsByType(CompilerTest.class)))
                .forEach(it -> register(compiler, it.classes(), it.classesIn(), it.options(), it.processors()));
        // Step three: CompilerTest annotation
        register(compiler, rootAnnotation.classes(), rootAnnotation.folders(), rootAnnotation.options(), rootAnnotation.processors());
        // Step four: Configuration on direct class
        Stream.of(testClass.getAnnotationsByType(CompilerTest.class))
                .forEach(it -> register(compiler, it.classes(), it.classesIn(), it.options(), it.processors()));
        // Step five: Configuration on methods
        Stream.of(extensionContext.getRequiredTestMethod().getAnnotationsByType(CompilerTest.class))
                .forEach(it -> register(compiler, it.classes(), it.classesIn(), it.options(), it.processors()));
    }

    private void register(
            Compiler compiler,
            String[] classes,
            String[] folders,
            String[] options,
            Class<? extends Processor>[] processors
    ) {
        for (String aClass : classes) {
            compiler.withClass(aClass);
        }

        for (String folder : folders) {
            compiler.withAllClassesFromFolder(folder);
        }

        for (String option : options) {
            compiler.withOption(option);
        }

        for (Class<? extends Processor> clazz : processors) {
            try {
                Processor processor = clazz.getConstructor().newInstance();
                compiler.withProcessor(processor);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new PreconditionViolationException("Could not construct the requested processor of type " + clazz + ". Please make sure that processor has a default constructor or add it manually.", e);
            }
        }
    }

    private static class CompilerState {
        private final Compiler compiler;
        private Compilation compilation;

        private CompilerState(Compiler compiler) {
            this.compiler = compiler;
        }

        public Compilation getCompilation() {
            if (compilation == null) {
                this.compilation = compiler.compile();
            }
            return compilation;
        }
    }
}
