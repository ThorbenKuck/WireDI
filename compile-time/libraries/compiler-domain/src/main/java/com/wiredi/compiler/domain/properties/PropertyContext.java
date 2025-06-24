package com.wiredi.compiler.domain.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wiredi.compiler.logger.slf4j.CompileTimeLogger;
import com.wiredi.compiler.logger.slf4j.CompileTimeLoggerFactory;
import org.slf4j.Logger;import com.wiredi.compiler.metainf.MetaInf;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;

public class PropertyContext {

    private static final CompileTimeLogger logger = CompileTimeLoggerFactory.getLogger(PropertyContext.class);
    private final ConfigurationMetadata configurationMetadata = new ConfigurationMetadata();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private final MetaInf metaInf;

    public PropertyContext(MetaInf metaInf) {
        this.metaInf = metaInf;
    }

    public void addProperty(String name, Consumer<ItemMetadata> itemConsumer) {
        logger.info(() -> "Adding property " + name + " to configuration metadata");
        configurationMetadata.addItem(ItemMetadata.ItemType.PROPERTY, name, itemConsumer);
    }

    public void addHint(String name, Consumer<ItemHint> itemConsumer) {
        configurationMetadata.addHint(name, itemConsumer);
    }

    public void addIgnore(ItemIgnore itemIgnore) {
        configurationMetadata.addIgnore(itemIgnore);
    }

    @PreDestroy
    public void write() {
        if (configurationMetadata.isEmpty()) {
            return;
        }
        logger.info("Flushing configuration metadata to META-INF/spring-configuration-metadata.json");
        try {
            String content = objectMapper.writeValueAsString(configurationMetadata);
            metaInf.writeFile(List.of(content), "spring-configuration-metadata.json");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write configuration metadata", e);
        }
    }
}
