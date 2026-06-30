package com.wiredi.annotations;

import org.jetbrains.annotations.Nullable;

/**
 * This enum value controls whether a class should be proxied or not.
 * <p>
 * It is set by the property {@code wiredi.proxy-mode} in your application configuration file.
 * <p>
 * Proxies are used to intercept method calls.
 * This is useful for different scenarios in which you'd like to enhance your application code with additional
 * functionality, without explicitly adding it to the business code.
 * In such a scenario aspects can be used to add additional functionality to the business code.
 * <p>
 * However, this functionality comes with a cost: performance degradation and memory consumption.
 * In general, the concept of WireDi is to design the application extensible in such a way that annotation processor
 * subroutines generate plugged-in code that is only executed if the corresponding feature is enabled.
 * This way, the application can be configured to run in production mode, where no proxies are used.
 * <p>
 * Though, still, some features just require proxies.
 * For example, if you want to use a feature similar to @Transactional or @Async straight from the application code,
 * you can use proxies to achieve this.
 * To not overburden the application with proxies that are not required, by default no proxies are generated except if
 * you explicitly set {@link Wire#proxy()} to true.
 * If you want to change this behavior, this enum value can be set and alter when and how proxies are generated.
 * <p>
 * Thanks to the AnnotationMetadata class (which is used by the annotation processor), this enum value can respect if
 * boolean fields in annotations are explicitly set or not.
 * And with that, this enum value can be used to control even though the {@link Wire#proxy()} field has a default value.
 * <p>
 * Each enum in this class defines a different behavior.
 * See the documentation of each enum value for details.
 */
public enum ProxyMode {

    /**
     * All classes that are explicitly setting {@link Wire#proxy()} to true will be proxied.
     * <p>
     * If used, no classes are proxied by default.
     * To proxy a class, explicitly set {@link Wire#proxy()} to true.
     * Setting {@link Wire#proxy()} to false has the same effect as not setting it at all.
     * <p>
     * These are all cases:
     *
     * <pre>
     * {@code
     * @Wire(proxy = true)
     * class IsProxied { }
     *
     * @Wire(proxy = false)
     * class IsNotProxied {}
     *
     * @Wire
     * class IsAlsoNotProxied {}
     * }
     * </pre>
     */
    OPT_IN {
        @Override
        public boolean shouldProxy(@Nullable Boolean proxy) {
            return proxy != null && proxy;
        }
    },
    /**
     * All classes will be proxied, except if they explicitly set {@link Wire#proxy()} to false.
     * <p>
     * If used, all classes are proxied by default.
     * To not proxy a class, explicitly set {@link Wire#proxy()} to false.
     * Setting {@link Wire#proxy()} to true has the same effect as not setting it at all.
     * <p>
     * These are all cases:
     *
     * <pre>
     * {@code
     * @Wire(proxy = true)
     * class IsProxied { }
     *
     * @Wire
     * class IsAlsoProxied {}
     *
     * @Wire(proxy = false)
     * class IsNotProxied {}
     * }
     * </pre>
     */
    OPT_OUT {
        @Override
        public boolean shouldProxy(@Nullable Boolean proxy) {
            return proxy == null || proxy;
        }
    },
    /**
     * No classes will be proxied by the annotation processor.
     * <p>
     * It does not matter if a class explicitly sets {@link Wire#proxy()} to true or false, a proxy will never be generated.
     */
    NONE {
        @Override
        public boolean shouldProxy(@Nullable Boolean proxy) {
            return false;
        }
    },
    /**
     * All classes will be proxied by the annotation processor.
     * <p>
     * It does not matter if a class explicitly sets {@link Wire#proxy()} to true or false, a proxy will always be generated.
     */
    ALL {
        @Override
        public boolean shouldProxy(@Nullable Boolean proxy) {
            return true;
        }
    };

    /**
     * A boolean method to determine if a class should be proxied or not.
     * <p>
     * This method can respect if a boolean field is explicitly set or not.
     * If the field is not set, the passed parameter {@code proxy} should be null.
     *
     * @param proxy whether the class should be proxied or not, or null if not explicitly set.
     * @return true if the class should be proxied, false otherwise.
     */
    public abstract boolean shouldProxy(@Nullable Boolean proxy);

}
