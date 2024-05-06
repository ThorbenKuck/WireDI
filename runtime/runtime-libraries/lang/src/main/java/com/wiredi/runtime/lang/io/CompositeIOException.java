package com.wiredi.runtime.lang.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompositeIOException extends IOException {

    private final List<IOException> composites;

    public CompositeIOException() {
        this.composites = new ArrayList<>();
    }

    public CompositeIOException(List<IOException> composites) {
        this.composites = new ArrayList<>(composites);
        composites.forEach(this::addSuppressed);
    }

    public CompositeIOException(List<IOException> composites, String message) {
        super(message);
        this.composites = new ArrayList<>(composites);
        composites.forEach(this::addSuppressed);
    }

    public List<IOException> getComposites() {
        return composites;
    }

    public CompositeIOException append(IOException ioException) {
        this.composites.add(ioException);
        addSuppressed(ioException);
        return this;
    }

    public boolean isEmpty() {
        return composites.isEmpty();
    }
}
