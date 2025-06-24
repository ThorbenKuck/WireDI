package com.wiredi.runtime.environment;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.ServiceFiles;
import com.wiredi.runtime.lang.Ordered;
import org.jetbrains.annotations.NotNull;

/**
 * A custom configuration for an Environment.
 * <p>
 * You can implement this interface and provide it to the {@link Environment}.
 * The configuration can be provided in one of two ways:
 * <ol>
 *     <li><b>ServiceLocation:</b> Add the fully qualified class name to the <code>META-INF/services/com.wiredi.runtime.environment.EnvironmentConfiguration</code> file. You can do so by manually adding this, or by using something like googles AUtoService.</li>
 *     <li><b>Wiring:</b> Provide it as a bean by annotating it with <code>@com.wiredi.annotations.Wire</code>.</li>
 * </ol>
 * The Environment itself will use the {@link ServiceFiles} to load {@code META-INF/services} files
 * when utilizing {@link Environment#autoconfigure}, whilst the {@link com.wiredi.runtime.WireRepository} will invoke
 * all instances when it is loaded.
 * <p>
 * This means that the {@code META-INF/services} files will always be applied before the bean instances and if you
 * manually use the Environment, you will only use the {@code META-INF/services} instances.
 * <p>
 * If an instance is both provided as a service and a bean, it will be applied twice.
 * <p>
 * Please note: Any implementation of this class should be stateless.
 * It is loaded once globally and hence any state will stay for multiple instances of different WireRepositories.
 * <p>
 * Additionally, services that are invoking {@link #configure(Environment)} should not invoke them in parallel.
 *
 * @see Environment
 * @see com.wiredi.runtime.WireRepository
 */
public interface EnvironmentConfiguration extends Ordered {

    /**
     * Configure the {@link EnvironmentConfiguration}.
     * <p>
     * All operations on the {@link Environment} are safe.
     *
     * @param environment the {@link Environment} that should be configured
     * @see Environment
     */
    void configure(@NotNull Environment environment);

}
