package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to signal a processor that the annotated class is wired automatically.
 * <p>
 * In certain configurations, such as with an annotation processor, there might be certain static analyze steps enabled.
 * One example might be, that an annotation processor might print a warning or even an error, when a wire candidate has
 * an injection point of a non-wire candidate. For example, let us imagine we have class A, which looks like this:
 *
 * <pre><code>
 * {@literal @}Wire
 * class A {
 *      {@literal @}Inject
 *      private B b;
 *
 *      // Other fields and methods
 * }
 * </code></pre>
 * <p>
 * and class B, which looks like this:
 *
 * <pre><code>
 * // Note: There is no {@literal @}Wire annotation on this class
 * public class B { ... }
 * </code></pre>
 * <p>
 * There might be a myriad of reasons, why one would not want to add the {@link Wire} annotation to their classes.
 * One example might be, that the class in question is maintained in another dependency container, but will be
 * available as a wire candidate in the WireRepository, because it is added by contract manually before the dependent
 * classes are requested. Like this for example:
 *
 * <pre><code>
 * public class Main {
 *     public static void main(String[] args) {
 *         WireRepository repository = WireRepository.open();
 *         OtherDependencyContainer other = OtherDependencyContainer.launch(Main.class);
 *
 *         B bInstance = other.getBean(B.class);
 *         repository.announce(bInstance);
 *         A a = repository.get(A.class);
 *     }
 * }
 * </code></pre>
 * <p>
 * The mechanism of how this is done exactly, is dependent on the integration of these frameworks. Compiling this with
 * the default annotation processor now yields the warning, that class A depends on the class B, which is not a wire
 * candidate, to catch potential errors while building applications. To disable this warning, you can add this
 * annotation to the manually maintained dependency (here class B) like this:
 *
 * <pre><code>
 * {@literal @}ManualWireCandidate
 * public class B { ... }
 * </code></pre>
 */
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface ManualWireCandidate {
}
