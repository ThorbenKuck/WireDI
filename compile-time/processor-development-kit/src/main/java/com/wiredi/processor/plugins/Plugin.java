package com.wiredi.processor.plugins;

import com.wiredi.domain.Ordered;

public interface Plugin extends Ordered {

    default void initialize() {}

}
