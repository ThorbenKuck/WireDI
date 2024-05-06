package com.wiredi.tests;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CapturedOutput {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final Consumer<CapturedOutput> close;

    public CapturedOutput(Consumer<CapturedOutput> close) {
        this.close = close;
    }

    public ByteArrayOutputStream out() {
        return outContent;
    }

    public ByteArrayOutputStream err() {
        return errContent;
    }

    public String getOutput() {
        return outContent.toString();
    }

    public String getError() {
        return errContent.toString();
    }

    public List<String> getOutputLines() {
        return Arrays.asList(outContent.toString().split(System.lineSeparator()));
    }

    public List<String> getErrorLines() {
        return Arrays.asList(errContent.toString().split(System.lineSeparator()));
    }

    protected void close() {
        close.accept(this);
    }
}
