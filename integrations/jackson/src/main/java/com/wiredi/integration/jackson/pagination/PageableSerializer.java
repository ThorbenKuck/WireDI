package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.wiredi.runtime.collections.pages.Pageable;

import java.io.IOException;

public class PageableSerializer extends StdSerializer<Pageable> {

    public PageableSerializer() {
        super(Pageable.class);
    }

    @Override
    public void serialize(Pageable value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("pageNumber", value.pageNumber());
        gen.writeNumberField("pageSize", value.pageSize());
        gen.writeObjectField("sort", value.sort());
        gen.writeEndObject();
    }
}
