package com.wiredi.annotations.scopes;

import java.lang.annotation.*;
import java.lang.reflect.Array;

/**
 * A metadata annotation for scope types.
 * <p>
 * This annotation can be added to custom scope annotation types to specify how the scope is initialized.
 * Let's say you'd wanted to create a custom scope for requests.
 * You can add this meta-annotation to your annotation and specify how the underlying scope is to be initialized.
 * For example, imagine that we have the following annotation:
 *
 * <pre>{@code
 * @jakarta.inject.Scope
 * @Retention(RetentionPolicy.CLASS)
 * @Target({ElementType.TYPE})
 * public @interface RequestScope {
 *
 * }
 * }</pre>
 * <p>
 * And now we have the following scope provider for a request scope:
 *
 * <pre>{@code
 * class RequestScopeProvider extends SimpleScopeProvider {
 *      public RequestScopeProvider() {
 *          super(ThreadLocal.class, () -> new ThreadLocalScope(false));
 *      }
 * }
 * }</pre>
 * <p>
 * Now we can make the RequestScope annotation aware about this {@code RequestScopeProvider}, by adding the ScopeMetadata:
 *
 * <pre>{@code
 * @jakarta.inject.Scope
 * @ScopeMetadata(scopeProvider = RequestScopeProvider.class) // This makes the annotation processor use our custom ScopeProvider
 * @Retention(RetentionPolicy.CLASS)
 * @Target({ElementType.TYPE})
 * public @interface RequestScope {
 *
 * }
 * }</pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface ScopeMetadata {

    /**
     * A String based initializer, if you want to use a custom
     *
     * @return
     */
    String scopeInitializer() default "";

    String[] imports() default {};

    Class<?> scopeProvider() default Void.class;

}
