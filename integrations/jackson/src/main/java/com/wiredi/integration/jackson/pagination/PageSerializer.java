package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.wiredi.runtime.collections.pages.Page;

import java.io.IOException;

public class PageSerializer extends StdSerializer<Page> {
    protected PageSerializer() {
        super(Page.class);
    }

    @Override
    public void serialize(Page value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeArrayFieldStart("content");
        for(Object o : value) {
            gen.writeObject(o);
        }
        gen.writeEndArray();
        gen.writeObjectField("pageable", value.getCurrentPage());
        gen.writeNumberField("totalPages", value.getTotalPages());
        gen.writeNumberField("totalElements", value.getTotalElements());
        gen.writeEndObject();
    }
}
