package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.wiredi.runtime.collections.pages.Order;
import com.wiredi.runtime.collections.pages.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SortDeserializer extends StdDeserializer<Sort> {

    public SortDeserializer() {
        super(Sort.class);
    }

    @Override
    public Sort deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode sort = jp.getCodec().readTree(jp);

        if (sort == null || sort.isNull()) {
            return Sort.unsorted();
        }
        List<Order> orders = new ArrayList<>();

        if (sort.isArray()) {
            for (JsonNode jsonNode : sort) {
                try (JsonParser traverse = jsonNode.traverse(jp.getCodec())) {
                    orders.add(traverse.readValueAs(Order.class));
                }
            }
        } else if (sort.isObject()) {
            JsonNode orderNode = sort.get("orders");
            if (orderNode == null || orderNode.isNull()) {
                try (JsonParser traverse = sort.traverse(jp.getCodec())) {
                    orders.add(traverse.readValueAs(Order.class));
                }
            } else if (orderNode.isArray()) {

                for (JsonNode jsonNode : sort) {
                    try (JsonParser traverse = jsonNode.traverse(jp.getCodec())) {
                        orders.add(traverse.readValueAs(Order.class));
                    }
                }
            }
        } else if (sort.isTextual()) {
            orders.add(Order.by(sort.asText()));
        } else {
            return null;
        }

        return Sort.by(orders);
    }
}
