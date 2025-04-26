package com.wiredi.runtime.lang.io;

import com.wiredi.runtime.lang.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CompositeOutputStream extends OutputStream {

    private final List<OutputStream> composites;

    public CompositeOutputStream(OutputStream... composites) {
        this.composites = (Arrays.asList(composites));
    }

    public CompositeOutputStream(Collection<OutputStream> composites) {
        this.composites = new ArrayList<>(composites);
    }

    @Override
    public void write(int b) throws IOException {
        forEach(composite -> composite.write(b));
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        forEach(composite -> composite.write(b));
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        forEach(composite -> composite.write(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        forEach(OutputStream::flush);
    }

    @Override
    public void close() throws IOException {
        forEach(OutputStream::close);
    }

    private void forEach(@NotNull ThrowingConsumer<@NotNull OutputStream, @NotNull IOException> consumer) throws IOException {
        final CompositeIOException compositeIOException = new CompositeIOException();
        for (OutputStream composite : this.composites) {
            try {
                consumer.accept(composite);
            } catch (IOException e) {
                compositeIOException.append(e);
            }
        }

        if (!compositeIOException.isEmpty()) {
            throw compositeIOException;
        }
    }
}
