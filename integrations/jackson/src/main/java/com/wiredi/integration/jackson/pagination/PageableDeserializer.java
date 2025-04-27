package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.wiredi.runtime.collections.pages.Order;
import com.wiredi.runtime.collections.pages.Pageable;
import com.wiredi.runtime.collections.pages.Paged;
import com.wiredi.runtime.collections.pages.Sort;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageableDeserializer extends StdDeserializer<Pageable> {

    public PageableDeserializer() {
        super(Pageable.class);
    }

    @Override
    public Pageable deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (isUnpaged(node)) {
            return Pageable.unpaged();
        }

        return new Paged(
                pageNumber(node),
                pageSize(node),
                sort(jp, node)
        );
    }

    private boolean isUnpaged(JsonNode node) {
        if (node.isEmpty()) {
            return true;
        }

        JsonNode paged = node.get("paged");
        if (paged == null) {
            return false;
        }

        if (paged.isNull()) {
            return true;
        }

        return !paged.asBoolean();
    }

    private int pageNumber(JsonNode node) {
        return node.get("pageNumber").asInt();
    }

    private int pageSize(JsonNode node) {
        return node.get("pageSize").asInt();
    }

    @NotNull
    private Sort sort(JsonParser root, JsonNode node) throws IOException {
        JsonNode sort = node.get("sort");
        if (sort == null || sort.isNull()) {
            return Sort.unsorted();
        } else {
            if (sort.isArray()) {
                List<Order> orders = new ArrayList<>();
                for (JsonNode order : sort) {
                    try (JsonParser traverse = order.traverse(root.getCodec())) {
                        orders.add(traverse.readValueAs(Order.class));
                    }
                }
                return Sort.by(orders);
            } else {
                try (JsonParser traverse = sort.traverse(root.getCodec())) {
                    return traverse.readValueAs(Sort.class);
                }
            }
        }
    }
}