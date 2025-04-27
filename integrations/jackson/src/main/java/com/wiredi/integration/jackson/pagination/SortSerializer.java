package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.wiredi.runtime.collections.pages.Order;
import com.wiredi.runtime.collections.pages.Sort;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SortSerializer extends StdSerializer<Sort> {

    protected SortSerializer() {
        super(Sort.class);
    }

    @Override
    public void serialize(Sort value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeBooleanField("sorted", value.isSorted());
        gen.writeArrayFieldStart("orders");
        for(Order order : value) {
            gen.writeObject(order);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    @NotNull
    private Sort sort(JsonParser jp, JsonNode node) throws IOException {
        JsonNode sort = node.get("sort");
        if (sort == null || sort.isNull()) {
            return Sort.unsorted();
        }
        List<Order> orders = new ArrayList<>();

        if (sort.isArray()) {
            sort.forEach(readOrder -> orders.add(readOrder((ObjectNode) readOrder)));
        } else if (sort.isObject()) {
            JsonNode orderNode = sort.get("orders");
            if (orderNode == null || orderNode.isNull()) {
                orders.add(readOrder((ObjectNode) sort));
            } else if (orderNode.isArray()) {
                orderNode.forEach(readOrder -> orders.add(readOrder((ObjectNode) readOrder)));
            }
        } else if (sort.isTextual()) {
            orders.add(Order.by(sort.asText()));
        } else {
            throw new IllegalStateException("Sort must be either an array or an object");
        }

        return Sort.by(orders);
    }

    private Order readOrder(ObjectNode node) {
        if (node.isTextual()) {
            return Order.by(node.asText());
        }

        if (node.isObject()) {

            Sort.Direction direction = Sort.DEFAULT_DIRECTION;
            boolean ignoreCase = Order.DEFAULT_IGNORE_CASE;
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

        throw new IllegalArgumentException("Order must be either a string or an object");
    }
}
