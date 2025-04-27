package com.wiredi.compiler;

import com.wiredi.compiler.logger.Logger;
import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.compiler.constructors.SingletonInstanceTypeConstructor;
import com.wiredi.compiler.constructors.TypeConstructor;
import com.wiredi.runtime.lang.ReflectionsHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * A simple dependency injector with limited JSR-330 support.
 * <p>
 * It is a primarily lazy injector, analyzing injection points when required.
 */
public class Injector {

    private static final Logger logger = Logger.get(Injector.class);
    private final TypeMap<TypeConstructor<?, ?>> constructors = new TypeMap<>();
    private final TypeMap<Class<?>> typeTranslations = new TypeMap<>();
    private final InjectorConfiguration configuration;

    public Injector() {
        this(new InjectorConfiguration());
    }

    public Injector(InjectorConfiguration configuration) {
        bind(Injector.class).toValue(this);
        this.configuration = configuration;
    }

    /**
     * Returns the configuration of the Injector.
     * <p>
     * This configuration is mutable.
     *
     * @return the configuration
     */
    public InjectorConfiguration configuration() {
        return configuration;
    }

    /**
     * Returns an instance of the type provided type.
     *
     * @param type the type of class you want to construct
     * @param <T>  the generic type of the class to construct
     * @return an instance of the provided type
     */
    @NotNull
    public <T> T get(@NotNull final Class<T> type) {
        return doGet(type, getCaller());
    }

    /**
     * Returns an instance of the type provided type.
     *
     * @param type the type of class you want to construct
     * @param <T>  the generic type of the class to construct
     * @return an instance of the provided type
     */
    @NotNull
    public <T> T doGet(@NotNull final Class<T> type, Class<?> caller) {
        @NotNull final TypeConstructor<T, T> typeConstructor = (TypeConstructor<T, T>) constructors.computeIfAbsent(type, () -> mewTypeConstructor(type, caller));
        return typeConstructor.construct(caller, type);
    }

    /**
     * Constructs a new {@link TypeConstructor} instance for the class.
     * <p>
     * The TypeConstructor can be a singleton, if the configuration determines the provided type to be singleton.
     *
     * @param type the type of which a TypeConstructor should be created.
     * @param <T>  the generic type of the class that should be created by the TypeCreated
     * @return a new TypeCreator for the type
     */
    private <T> TypeConstructor<T, T> mewTypeConstructor(@NotNull final Class<T> type, Class<?> caller) {
        @NotNull TypeConstructor<T, T> typeConstructor = TypeConstructor.wrap(() -> createNewInstanceOf(type, caller));

        if (configuration.isSingleton(type)) {
            typeConstructor = typeConstructor.asSingleton();
        }

        return typeConstructor;
    }

    /**
     * Post processes any instance.
     * <p>
     * This will do two things:
     * <ul>
     *     <li>Find field injections and invoke these</li>
     *     <li>Find method injections and invoke these</li>
     *     <li>Find post construct methods and invoke these</li>
     * </ul>
     *
     * @param instance The instance you want to post process
     * @param <T>      the generic of the Object, supporting chains
     * @return the provided instance, but all field and method injections and post construct methods have been called
     */
    public <T> T postProcess(T instance) {
        return postProcess(instance, getCaller());
    }

    /**
     * Post processes any instance.
     * <p>
     * This will do two things:
     * <ul>
     *     <li>Find field injections and invoke these</li>
     *     <li>Find method injections and invoke these</li>
     *     <li>Find post construct methods and invoke these</li>
     * </ul>
     * <p>
     * This method is "caller sensitive".
     * Instead of resolving the caller of this method using reflection, it is provided manually.
     *
     * @param instance The instance you want to post process
     * @param caller   The caller of this method
     * @param <T>      the generic of the Object, supporting chains
     * @return the provided instance, but all field and method injections and post construct methods have been called
     */
    public <T> T postProcess(T instance, Class<?> caller) {
        if (instance == null) {
            return null;
        }

        configuration.findAllInjectionFields(instance).forEach(field -> {
            Object fieldInstance = get(field.getType());
            ReflectionsHelper.setField(instance, field, fieldInstance);
        });

        if (CallerAware.class.isAssignableFrom(instance.getClass())) {
            ((CallerAware) instance).setCaller(caller);
        }

        configuration.findAllInjectionMethods(instance).forEach(method -> {
            ReflectionsHelper.invokeMethod(instance, method, getArgumentsFor(method));
        });

        configuration.findAllPostConstructMethods(instance).forEach(method -> {
            ReflectionsHelper.invokeMethod(instance, method, getArgumentsFor(method));
        });

        return instance;
    }

    /**
     * Returns a new object with all parameters for a method.
     * <p>
     * These parameters will be constructed by the {@link #get(Class)} method.
     *
     * @param method the method for which you want to find parameters
     * @return all parameters
     */
    public Object[] getArgumentsFor(Method method) {
        return Arrays.stream(method.getParameters())
                .map(it -> get(it.getType()))
                .toArray();
    }

    /**
     * Clears all local states.
     * <p>
     * Note: This method does not clear the state of the configurations.
     */
    public void clear() {
        typeTranslations.clear();
        constructors.clear();
    }

    /**
     * Binds the provided type to any resolver you would like to have
     *
     * @param type the type to bind
     * @param <T>  The generic of the type.
     * @return a new bind stage to specify what the type should be bound to.
     */
    public <T> BindStage<T> bind(Class<T> type) {
        return new BindStage<>(type, this);
    }

    private <T> T createNewInstanceOf(Class<T> type, Class<?> caller) {
        var translatedType = (Class<T>) typeTranslations.getOrDefault(type, type);
        T instance = resolveAndConstructInstanceOf(translatedType);
        postProcess(instance, caller);
        return instance;
    }

    private <T> T resolveAndConstructInstanceOf(Class<T> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();

        if (constructors.length == 0) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        Constructor<?> constructor;
        if (constructors.length == 1) {
            constructor = constructors[0];
        } else {
            var annotatedConstructors = Arrays.stream(constructors)
                    .filter(configuration::isInjectionPoint)
                    .toList();

            if (annotatedConstructors.size() != 1) {
                throw new IllegalStateException("Please provide a single constructor with @Inject on " + type);
            }
            constructor = annotatedConstructors.get(0);
        }

        var args = Arrays.stream(constructor.getParameterTypes())
                .map(it -> doGet(it, type))
                .toList()
                .toArray();
        try {
            constructor.trySetAccessible();
            return (T) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private Class<?> getCaller() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        boolean match = false;
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (match) {
                try {
                    return Class.forName(ste.getClassName());
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            } else if (ste.getClassName().equals(Injector.class.getName()) && (ste.getMethodName().equals("get") || ste.getMethodName().equals("postProcess"))) {
                match = true;
            }
        }

        return null;
    }

    public class BindStage<T> {
        private final Class<T> type;
        private final Injector injector;

        public BindStage(Class<T> type, Injector injector) {
            this.type = type;
            this.injector = injector;
        }

        public <S extends T> Injector toType(Class<S> other) {
            injector.typeTranslations.put(type, other);
            return injector;
        }

        public <S extends T> S toValue(S instance) {
            injector.constructors.put(type, new SingletonInstanceTypeConstructor<>(() -> postProcess(instance)));
            return instance;
        }

        public <S extends T> Injector toInstance(S instance) {
            if (injector.configuration.isSingleton(instance.getClass())) {
                injector.constructors.put(type, new SingletonInstanceTypeConstructor<>(() -> postProcess(instance)));
            }
            return injector;
        }

        public <S extends T> Injector toConstructor(TypeConstructor<T, S> instance) {
            injector.constructors.put(type, new FieldInjectionSupport<>(instance));
            return injector;
        }

        public <S extends T> Injector toSingleton(TypeConstructor<T, S> instance) {
            injector.constructors.put(type, new FieldInjectionSupport<>(instance).asSingleton());
            return injector;
        }

        public <S extends T> Injector toSupplier(Supplier<S> supplier) {
            return toConstructor(TypeConstructor.wrap(supplier));
        }
    }

    private class FieldInjectionSupport<T, S extends T> implements TypeConstructor<T, S> {

        private final TypeConstructor<T, S> delegate;

        private FieldInjectionSupport(TypeConstructor<T, S> delegate) {
            this.delegate = delegate;
        }

        @Override
        public S construct(Class<?> caller, Class<T> type) {
            S construct = delegate.construct(caller, type);
            postProcess(construct);
            return construct;
        }
    }
}
