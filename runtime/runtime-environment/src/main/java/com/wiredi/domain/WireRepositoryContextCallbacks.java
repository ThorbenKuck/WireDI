package com.wiredi.domain;

import com.wiredi.aspects.AspectRepository;
import com.wiredi.environment.Environment;
import com.wiredi.lang.time.Timed;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.beans.BeanContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class is used by the {@link WireRepository} to
 * inform about context actions.
 * <p>
 * Since this class is existing before the {@link BeanContainer}
 * and the {@link Environment} is loaded, it does <b>NOT</b>
 * support any form of injection. Neither  field nor constructor
 * injections. Any implementations  of this interface must be
 * instantiable by q  {@link java.util.ServiceLoader}, meaning
 * that it must have a single, no-arg constructor.
 * </p>
 * <p>
 * Any instance of this class will be called during the load lifecycle.
 * </p>
 * <2>The lifecycle</2>
 * <p>
 * The general load lifecycle of the {@link WireRepository} is as follows:
 * </p>
 * <p>
 * 1) Loading the {@link Environment}.
 * </p>
 * <p>
 * The loading process starts with loading the environment.
 * This means resolving properties and other preparing an instance
 * of the {@link Environment}.
 * </p>
 * <p>
 * 2) Loading the {@link BeanContainer}.
 * </p>
 * <p>
 * The {@link BeanContainer} will be loaded. During this step,
 * all identifiable provider will be loaded from the {@link java.util.ServiceLoader},
 * as in the general contract of this framework. Each callback can
 * choose to interact with the {@link BeanContainer} to append beans.
 * </p>
 * <p>
 * 3) Loading the AspectRepository
 * </p>
 * <p>
 * As the BeanContainer loaded all WiredClasses, the {@link com.wiredi.domain.aop.AspectAwareProxy}
 * will only work once the AspectRepository is loaded. This is also the step,
 * during which additional and external aspects can be added to the
 * {@link AspectRepository}. It is not required to do so inside of a
 * callback, but it can be done.
 * </p>
 * <p>
 * 4) Instantiating Eager classes
 * </p>
 * <p>
 * All Bean instances of the {@link Eager} interface, which were
 * loaded inside of the {@link BeanContainer}. All BeanContainers
 * will be executed in parallel.
 * </p>
 * <p>
 * Any error raised by an instance of this interface will be handled by the
 * default error handling mechanism, using {@link com.wiredi.domain.errors.ErrorHandler}
 * instances, that can be found in the {@link BeanContainer}. If the
 * {@link BeanContainer} is not yet configured, the exception will
 * simply be delegated.
 *
 * @see LoggingWireRepositoryContextCallbacks
 * @see Environment
 * @see BeanContainer
 * @see AspectRepository
 * @see Eager
 * @see WireRepository
 */
public interface WireRepositoryContextCallbacks extends Ordered {

	/**
	 * This method will be called after the banner has been printed,
	 * but before the environment is configured.
	 * <p>
	 * During this step, any callback can configure the WireRepository
	 * to its liking. Especially for configuring the {@link Environment}
	 * before it is loaded.
	 *
	 * @param wireRepository the WireRepository that is being loaded
	 */
	default void loadingStarted(@NotNull WireRepository wireRepository) {
	}

	/**
	 * This method will be called after the first phase of the load lifecycle
	 * (loading the {@link Environment}) has successfully been concluded and
	 * before the second phase, loading the {@link BeanContainer} has been
	 * started.
	 *
	 * @param timed       The time that loading the environment required
	 * @param environment the fully configured environment.
	 */
	default void loadedEnvironment(@NotNull Timed timed, @NotNull Environment environment) {
	}

	/**
	 * This method will be called after the second phase of the load lifecycle
	 * (loading the {@link BeanContainer}) has successfully been concluded and
	 * before the third phase, loading the {@link AspectRepository} has been
	 * started.
	 *
	 * @param timed         The time that loading the BeanContainer required
	 * @param beanContainer the fully configured BeanContainer
	 */
	default void loadedBeanContainer(@NotNull Timed timed, @NotNull BeanContainer beanContainer) {
	}

	/**
	 * This method will be called after the third phase of the load lifecycle
	 * (loading the {@link AspectRepository}) has successfully been concluded and
	 * before the fourth and last phase, loading the {@link Eager EagerInstances}
	 * has been started.
	 *
	 * @param timed            The time that loading the AspectRepository required
	 * @param aspectRepository the fully configured AspectRepository
	 */
	default void loadedAspectRepository(@NotNull Timed timed, @NotNull AspectRepository aspectRepository) {
	}

	/**
	 * This method will be called after the last phase of the load lifecycle
	 * (initializing the {@link Eager EagerInstances}) has successfully been
	 * concluded.
	 * <p>
	 * This method will only be called, if at least one eager class is present
	 * in the previously configured {@link WireRepository}. If not eager class
	 * is present, instead only the next method {@link #loadingFinished(Timed, WireRepository)}
	 * will be called.
	 *
	 * @param timed          The time that loading the AspectRepository required
	 * @param eagerInstances the fully configured AspectRepository
	 */
	default void loadedEagerClasses(@NotNull Timed timed, @NotNull List<? extends Eager> eagerInstances) {
	}

	/**
	 * This method will be called, after the loading lifecycle of the {@link WireRepository}
	 * has successfully concluded.
	 *
	 * @param timed          the total time that loading the WireRepository took
	 * @param wireRepository the fully configured {@link WireRepository}
	 */
	default void loadingFinished(@NotNull Timed timed, @NotNull WireRepository wireRepository) {
	}
}
