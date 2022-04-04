package com.github.thorbenkuck.di.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface ManualWireCandidate {
}
