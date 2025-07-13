package com.wiredi.compiler.processor.plugins;

import com.wiredi.compiler.Injector;
import com.wiredi.runtime.lang.Ordered;

public interface Plugin extends Ordered {

    default void initialize(Injector injector) {}

}
