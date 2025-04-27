package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.wiredi.runtime.collections.pages.Order;

import java.io.IOException;

public class OrderSerializer extends StdSerializer<Order> {

    public OrderSerializer() {
        super(Order.class);
    }

    @Override
    public void serialize(Order value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("property", value.property());
        gen.writeEndObject();
    }
}
