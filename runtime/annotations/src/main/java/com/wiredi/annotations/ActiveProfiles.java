package com.wiredi.annotations;

import com.wiredi.annotations.documentation.Generates;

import java.lang.annotation.*;

/**
 * This annotation activates profiles, similar to the property <code>active.profiles</code>.
 * <p>
 * In the default SDK, this annotation is consumed by the `ActiveProfilesWireProcessor`,
 * which can be found `processors` module and therefore not linked here.
 * The annotation processor generates an EnvironmentConfiguration which updates the environment.
 * It adds the {@link #value()} entries to the active profiles property of the environment.
 * <p>
 * This annotation isn't meant to be the main way to control active profiles.
 * You'd normally control the profile through environment configuration or OS properties.
 * <p>
 * Note that active profiles here are added to all active profiles already active.
 * So, if the profile "local" is activated through properties, and you add an annotation with the profile "test",
 * both local and test are active profiles.
 * <p>
 * By default, the generated class will have the order {@link Order#FIRST}.
 * If the annotated class is also annotated with {@link Order}, the provided order will be taken.
 * <p>
 * However, as the generated class is an EnvironmentConfiguration, it doesn't respect conditional annotations.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Documented
@Inherited
@Generates(
        classes = {"EnvironmentConfiguration"},
        byAnnotationProcessors = {"ActiveProfilesWirerProcessor"}
)
public @interface ActiveProfiles {

    /**
     * All profiles to activate in addition to any other profiles.
     *
     * @return all additional profiles to activate.
     */
    String[] value();

}
