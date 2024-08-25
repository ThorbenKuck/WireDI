package com.wiredi.runtime.banner;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.resources.Resource;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Banner {

    public static final Key BANNER_ENRICHMENT_PROPERTY = Key.just("banner.enrich");
    public static final Key BANNER_MODE_PROPERTY = Key.just("banner.display-mode");
    public static final Key BANNER_LOCATION = Key.just("banner.location");
    public static final String CLASSPATH_BANNER_LOCATION = "classpath:banner.txt";

    @NotNull
    private Environment environment;
    private final Value<List<String>> bannerContent = Value.lazy(this::loadBanner);
    private final Value<BannerPrinter> printer = Value.lazy(() -> environment.getProperty(BANNER_MODE_PROPERTY, BannerMode.class, BannerMode.CONSOLE));

    public Banner(@NotNull Environment environment) {
        this.environment = environment;
    }

    public Banner setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public Banner setPrinter(BannerPrinter printer) {
        this.printer.set(printer);
        return this;
    }

    public void print() {
        BannerPrinter printer = this.printer.get();
        if (printer.willPrint()) {
            printer.print(bannerContent.get());
        }
    }

    private List<String> loadBanner() {
        String property = environment.getProperty(BANNER_LOCATION, CLASSPATH_BANNER_LOCATION);
        Resource resource = environment.loadResource(property);
        if (resource.doesNotExist()) {
            throw new ExceptionInInitializerError("Unable to load banner from " + resource);
        }

        try {
            return enrichWithProperties(resource);
        } catch (final IOException e) {
            ExceptionInInitializerError error = new ExceptionInInitializerError("Unable to load banner from " + resource);
            error.addSuppressed(e);
            throw error;
        }
    }

    private List<String> enrichWithProperties(
            @NotNull final Resource resource
    ) throws IOException {
        boolean enrich = environment.getProperty(BANNER_ENRICHMENT_PROPERTY, true);
        List<String> result = new ArrayList<>();
        try (BufferedReader bufferedInputStream = new BufferedReader(resource.openReader())) {
            while (bufferedInputStream.ready()) {
                String line = bufferedInputStream.readLine();
                if (enrich) {
                    line = environment.resolve(line);
                }
                result.add(line + System.lineSeparator());
            }
        }

        return result;
    }
}
