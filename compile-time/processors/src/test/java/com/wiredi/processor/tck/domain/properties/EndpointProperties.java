package com.wiredi.processor.tck.domain.properties;

import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertyBinding;

@PropertyBinding(prefix = "endpoint")
public class EndpointProperties {

    private final String one;
    private final String oneWithName;

    public EndpointProperties(
            String one,
            @Property(name = "one") String oneWithName
    ) {
        this.one = one;
        this.oneWithName = oneWithName;
    }

    public String getOne() {
        return one;
    }

    public String getOneWithName() {
        return oneWithName;
    }
}
