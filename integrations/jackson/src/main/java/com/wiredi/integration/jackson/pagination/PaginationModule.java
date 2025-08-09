package com.wiredi.integration.jackson.pagination;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.wiredi.runtime.collections.pages.Order;
import com.wiredi.runtime.collections.pages.Page;
import com.wiredi.runtime.collections.pages.Pageable;
import com.wiredi.runtime.collections.pages.Sort;

public class PaginationModule extends SimpleModule {
    public PaginationModule() {
        super("PaginationModule", new Version(1, 0, 0, null, "com.wiredi", "jackson-integration"));
        addDeserializer(Pageable.class, new PageableDeserializer());
        addDeserializer(Page.class, new PageDeserializer());
        addDeserializer(Order.class, new OrderDeserializer());
        addDeserializer(Sort.class, new SortDeserializer());

        addSerializer(Pageable.class, new PageableSerializer());
        addSerializer(Page.class, new PageSerializer());
        addSerializer(Order.class, new OrderSerializer());
        addSerializer(Sort.class, new SortSerializer());
    }
}
