package com.wiredi.processor.tck.domain.properties;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.properties.Resolve;
import com.wiredi.domain.Eager;
import com.wiredi.runtime.WireRepository;

@Wire
public class ProfileProperties implements Eager {

    private final String property;

    public ProfileProperties(@Resolve("${test.value}") String property) {
        this.property = property;
    }

    @Override
    public void setup(WireRepository wireRepository) {
        System.out.println(property);
    }
}
