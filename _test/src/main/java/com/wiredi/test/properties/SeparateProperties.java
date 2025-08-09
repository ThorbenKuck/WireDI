package com.wiredi.test.properties;

import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertyBinding;

@PropertyBinding(file = "classpath:separate.properties")
public record SeparateProperties(
        String foo,
        @Property(defaultValue = "3")
        double pi
) {

    @Override
    public String toString() {
        return "SeparateProperties{" +
                "foo='" + foo + '\'' +
                ", pi=" + pi +
                '}';
    }
}
