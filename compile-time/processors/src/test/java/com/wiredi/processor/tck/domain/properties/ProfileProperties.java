package com.wiredi.processor.tck.domain.properties;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.environment.Resolve;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.Eager;

@Wire
public class ProfileProperties implements Eager {

    private final String property;

    public ProfileProperties(@Resolve("${test.value}") String property) {
        this.property = property;
    }

    @Override
    public void setup(WireContainer wireRepository) {
        System.out.println(property);
    }
}
