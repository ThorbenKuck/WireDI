package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.wiredi.runtime.collections.pages.ListPage;
import com.wiredi.runtime.collections.pages.Page;
import com.wiredi.runtime.collections.pages.Pageable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PageDeserializer extends StdDeserializer<Page<?>> implements ContextualDeserializer {

    private JavaType generic;

    public PageDeserializer() {
        super(Page.class);
    }

    @Override
    public Page<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        JavaType generic = Optional.ofNullable(this.generic).orElseGet(() -> ctxt.getContextualType().containedType(0));
        JsonNode pageNode = jp.getCodec().readTree(jp);
        JsonNode contentNode = pageNode.get("content");
        JsonNode pageableNode = pageNode.get("pageable");
        Pageable pageable;
        List<Object> content = new ArrayList<>();
        try (JsonParser contentParser = pageableNode.traverse(jp.getCodec())) {
            pageable = contentParser.readValueAs(Pageable.class);
        }

        for (JsonNode entry : contentNode) {
            ctxt.readTreeAsValue(entry, generic);
        }

        return new ListPage<>(
                pageNode.get("totalPages").asInt(),
                pageNode.get("totalElements").asLong(),
                content,
                pageable
        );
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        PageDeserializer pageDeserializer = new PageDeserializer();
        pageDeserializer.generic = ctxt.getContextualType().containedType(0);
        return pageDeserializer;
    }
}
