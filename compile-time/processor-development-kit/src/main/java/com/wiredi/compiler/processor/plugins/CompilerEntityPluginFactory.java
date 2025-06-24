package com.wiredi.compiler.processor.plugins;

import com.wiredi.compiler.Injector;
import com.wiredi.runtime.lang.Ordered;

public interface CompilerEntityPluginFactory extends Ordered {

    CompilerEntityPlugin create(Injector injector);

}
