package com.wiredi.tests;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.io.CompositeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A class to capture outputs to {@link System#out} and {@link System#err}.
 * <p>
 * This class itself is stateless, constructing a {@link CapturedOutput} instance once capturing.
 * Upon calling {@link #capture()}, the {@link System#out} and {@link System#err} PrintStreams will be replaced with
 * {@link ByteArrayOutputStream} of the linked {@link CapturedOutput}.
 * They can be used to test for outputs.
 * <p>
 * Once closed, the {@link CapturedOutput} is reset and can be reused again later.
 *
 * @see CaptureOutput
 * @see CaptureOutputExtension
 * @see CapturedOutput
 */
public class OutputCollector {

    private static final Object lock = new Object();

    /**
     * Will capture all output in the provided {@code runnable}.
     * <p>
     * This method is functionally the same as {@link #capture(ThrowingRunnable)}, but more readable in static imports.
     *
     * @param runnable the runnable from which to capture the output.
     * @param <T>      the generic of potential throwable that could be thrown
     * @return the captured output of the {@code runnable}
     * @throws T the throwable
     * @see #capture(ThrowingRunnable)
     */
    public static <T extends Throwable> CapturedOutput captureOutputOf(ThrowingRunnable<T> runnable) throws T {
        return capture(runnable);
    }

    /**
     * Will capture all output in the provided {@code runnable}.
     * <p>
     * Exceptions raised in this method will be passed through.
     * The resulting {@link CapturedOutput} will hold the result of all {@link System#out} and {@link System#err} calls.
     *
     * @param runnable the runnable from which to capture the output.
     * @param <T>      the generic of potential throwable that could be thrown
     * @return the captured output of the {@code runnable}
     * @throws T the throwable
     * @see #captureOutputOf(ThrowingRunnable)
     */
    public static <T extends Throwable> CapturedOutput capture(ThrowingRunnable<T> runnable) throws T {
        OutputCollector collector = new OutputCollector();
        CapturedOutput capture = collector.capture();
        try {
            runnable.run();
            return capture;
        } finally {
            capture.close();
        }
    }

    /**
     * Starts to capture all calls to {@link System#out} and {@link System#err}.
     *
     * @return a new {@link CapturedOutput}
     */
    public CapturedOutput capture() {
        return capture(true);
    }

    /**
     * Starts to capture all calls to {@link System#out} and {@link System#err}.
     * <p>
     * If {@code suppressOriginal} is true, the original {@link System#out} and {@link System#err} will not be used.
     * On false, a {@link CompositeOutputStream} will be used that is delegating all calls to {@link System#out} and
     * {@link System#err} to both the original and the {@link CapturedOutput}.
     * <p>
     * Set this to false if you still want to see logs in your tests, even though you are capturing the output.
     * Otherwise, pass true to this method. In this case, you will not see any output to the console.
     *
     * @param suppressOriginal whether the original output should be suppressed or not
     * @return a new {@link CapturedOutput}
     */
    public CapturedOutput capture(boolean suppressOriginal) {
        synchronized (lock) {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;

            CapturedOutput output = new CapturedOutput(it -> {
                System.setOut(originalOut);
                System.setErr(originalErr);
            });

            if (suppressOriginal) {
                System.setOut(new PrintStream(output.out()));
                System.setErr(new PrintStream(output.err()));
            } else {
                System.setOut(new PrintStream(new CompositeOutputStream(originalOut, output.out())));
                System.setErr(new PrintStream(new CompositeOutputStream(originalErr, output.err())));
            }

            return output;
        }
    }

    public CapturedOutput capture(boolean suppressSystemOut, boolean suppressSystemErr) {
        synchronized (lock) {
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;

            CapturedOutput output = new CapturedOutput(it -> {
                System.setOut(originalOut);
                System.setErr(originalErr);
            });

            if (suppressSystemOut) {
                System.setOut(new PrintStream(output.out()));
            } else {
                System.setOut(new PrintStream(new CompositeOutputStream(originalOut, output.out())));
            }

            if (suppressSystemErr) {
                System.setErr(new PrintStream(output.err()));
            } else {
                System.setErr(new PrintStream(new CompositeOutputStream(originalErr, output.err())));
            }

            return output;
        }
    }

    public void close(CapturedOutput capturedOutput) {
        capturedOutput.close();
    }
}
