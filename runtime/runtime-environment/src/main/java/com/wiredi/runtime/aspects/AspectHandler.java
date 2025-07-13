package com.wiredi.runtime.aspects;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.domain.aop.AspectAwareProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Any instance of this interface, are treated as a handler for a specific
 * {@link com.wiredi.annotations.aspects.AspectTarget aspectTarget} instance.
 * <p>
 * You can provide a handler for aspect enabled annotations, by adding it to the wire container,
 * which means you can write a class like this:
 *
 * <pre><code>
 * {@literal @}Wire
 * public class NotNullAspectHandler implements AspectHandler{@literal <}NotNull> {
 *      {@literal @}Override
 *      public Object process(ExecutionContext{@literal <}NotNull> context) {
 *          Object returnValue = context.proceed();
 *          if (returnValue == null) {
 *              throw new NullPointerException("The function may not return null!");
 *          }
 *      }
 * }
 * </code></pre>
 * <p>
 * It is important, that this class is annotated with {@link com.wiredi.annotations.Wire @Wire}, or
 * manually added to the {@link WireContainer} before loading classes. Only then
 * will it be picked up and inserted into the {@link AspectAwareProxy Proxy} instances.
 * <p>
 * The second possibility is, that you annotate a handler method in any wire bean with
 * {@link com.wiredi.annotations.aspects.Aspect @Aspect}, like this:
 *
 * <pre><code>
 * {@literal @}Wire
 * public class NotNullAspectHandler {
 *      {@literal @}Aspect(around = NotNull.class)
 *      public Object process(ExecutionContext{@literal <}NotNull> context, MyService myService) {
 *          Object returnValue = context.proceed();
 *          if (returnValue == null) {
 *              myService.raiseNullPointer(); // Here we use a custom bean to raise the exception
 *          }
 *      }
 * }
 * </code></pre>
 * <p>
 * In the second approach, i.e. annotating a method with {@literal @}Aspect, you can define any
 * number of method parameters, that will be injected from the WireRepository. At compile time,
 * an AnnotationProcessor will pick up the wired bean and construct an instance of the
 * {@link AspectHandler} interface for you.
 * <p>
 * The aspect approach is following a chain of responsibility pattern, where each aspect handler must
 * explicitly continue the chain, by stating <code>context.proceed()</code>. If not done, the
 * chain will not be continued. The last element in the chain (the tail) is always the root method,
 * meaning a super call to the original method. If your handler does not call <code>context.proceed()</code>,
 * the original method will never be invoked.
 *
 * @see com.wiredi.annotations.aspects.Aspect
 * @see com.wiredi.annotations.aspects.AspectTarget
 * @see ExecutionContext
 * @see ExecutionChain
 * @see ExecutionChainRegistry
 * @see ExecutionChainParameters
 */
@FunctionalInterface
public interface AspectHandler extends Ordered {

	@NotNull
	static AspectHandler wrap(@NotNull Consumer<ExecutionContext> consumer) {
		return new AspectHandlerConsumerWrapper(consumer);
	}

	@Nullable
	Object process(@NotNull final ExecutionContext context);

	/**
	 * This method describes if an AspectHandler instance should be used in an ExecutionChain
	 *
	 * @param rootMethod
	 * @return
	 */
	default boolean appliesTo(@NotNull RootMethod rootMethod) {
		return true;
	}
}
