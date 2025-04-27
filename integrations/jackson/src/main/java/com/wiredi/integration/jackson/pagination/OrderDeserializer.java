package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.wiredi.runtime.collections.pages.Order;
import com.wiredi.runtime.collections.pages.Sort;

import java.io.IOException;

public class OrderDeserializer extends StdDeserializer<Order> {

    public OrderDeserializer() {
        super(Order.class);
    }

    @Override
    public Order deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.isTextual()) {
            return Order.by(node.asText());
        }

        if (node.isObject()) {
            JsonNode propertyNode = node.get("property");
            if (propertyNode == null || propertyNode.isNull()) {
                throw new IllegalArgumentException("Property is required for ordering");
            }
            Order.Builder builder = Order.builder(propertyNode.asText());

            JsonNode directionNode = node.get("direction");
            if (directionNode != null && !directionNode.isNull()) {
                builder.withDirection(Sort.Direction.parse(directionNode.asText()));
            }

            JsonNode ignoreCaseNode = node.get("ignoreCase");
            if (ignoreCaseNode != null && !ignoreCaseNode.isNull()) {
                builder.withIgnoreCase(ignoreCaseNode.asBoolean());
            }

            JsonNode nullHandlingNode = node.get("nullHandling");
            if (nullHandlingNode != null && !nullHandlingNode.isNull()) {
                builder.withNullHandling(Sort.NullHandling.valueOf(nullHandlingNode.asText()));
            }

            return builder.build();
        }

        return null;
    }
}
