package com.wiredi.runtime.benchmark;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.TimedValue;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utility class for benchmarking WireContainer performance in production environments.
 * 
 * This class provides methods for timing container operations and generating performance reports.
 * It's designed to be used during application startup or during runtime to collect performance
 * metrics that can guide optimization efforts.
 */
public final class WireContainerBenchmarkUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Path DEFAULT_REPORTS_DIR = Paths.get("logs", "benchmarks");

    private WireContainerBenchmarkUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Runs a benchmark on a WireContainer operation and returns timing information.
     *
     * @param name A descriptive name for the benchmark
     * @param operation The operation to benchmark
     * @param warmupIterations Number of warmup iterations to run before measuring
     * @param measurementIterations Number of measurement iterations to run
     * @return A BenchmarkResult containing timing information
     */
    public static <T extends Throwable> BenchmarkResult runBenchmark(String name, ThrowingRunnable<T> operation, int warmupIterations, int measurementIterations) throws T {
        // Warmup
        for (int i = 0; i < warmupIterations; i++) {
            operation.run();
        }

        // Measurement
        List<Duration> measurements = new ArrayList<>();
        for (int i = 0; i < measurementIterations; i++) {
            Timed timed = Timed.of(operation);
            measurements.add(timed.duration());
        }

        return new BenchmarkResult(
                name,
                calculateAverage(measurements),
                calculateMinimum(measurements),
                calculateMaximum(measurements)
        );
    }

    /**
     * Benchmarks the loading time of a WireContainer.
     * 
     * @param containerSupplier Supplier that creates and loads a WireContainer
     * @param warmupIterations Number of warmup iterations to run before measuring
     * @param measurementIterations Number of measurement iterations to run
     * @return A BenchmarkResult containing timing information about container loading
     */
    public static BenchmarkResult benchmarkContainerLoading(
            Supplier<WireContainer> containerSupplier,
            int warmupIterations,
            int measurementIterations
    ) {
        // Warmup phase
        for (int i = 0; i < warmupIterations; i++) {
            containerSupplier.get();
        }

        // Measurement phase
        List<Duration> measurements = new ArrayList<>();
        List<WireContainer> containers = new ArrayList<>(); // Keep references to prevent GC during measurement

        for (int i = 0; i < measurementIterations; i++) {
            TimedValue<WireContainer> timed = Timed.of(() -> {
                WireContainer container = containerSupplier.get();
                containers.add(container);
                return container;
            });
            measurements.add(timed.duration());
        }

        return new BenchmarkResult(
                "WireContainer Loading",
                calculateAverage(measurements),
                calculateMinimum(measurements),
                calculateMaximum(measurements)
        );
    }

    /**
     * Generates a CSV report from a list of benchmark results.
     * 
     * @param reportName The base name for the report file
     * @param results The benchmark results to include in the report
     * @param reportDir Optional custom directory for the report, or null to use default
     * @return The path to the generated report file
     * @throws IOException If there's an error writing the report
     */
    public static Path generateReport(String reportName, List<BenchmarkResult> results, Path reportDir) throws IOException {
        // Create reports directory if it doesn't exist
        Path reportsDirectory = reportDir != null ? reportDir : DEFAULT_REPORTS_DIR;
        Files.createDirectories(reportsDirectory);

        // Create filename with timestamp
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        Path reportFile = reportsDirectory.resolve(reportName + "-" + timestamp + ".csv");

        // Write CSV
        try (FileWriter writer = new FileWriter(reportFile.toFile())) {
            // Write header
            writer.write("operation,avgTimeMs,minTimeMs,maxTimeMs\n");

            // Write data
            for (BenchmarkResult result : results) {
                writer.write(String.format("%s,%.2f,%.2f,%.2f\n",
                        result.operationName(),
                        result.averageTime().toMillis(),
                        result.minimumTime().toMillis(),
                        result.maximumTime().toMillis()));
            }
        }

        return reportFile;
    }

    /**
     * Calculates the average of a list of durations
     */
    private static Duration calculateAverage(List<Duration> durations) {
        return Duration.ofNanos((long) durations.stream()
                .mapToLong(Duration::toNanos)
                .average()
                .orElse(0));
    }

    /**
     * Calculates the minimum of a list of durations
     */
    private static Duration calculateMinimum(List<Duration> durations) {
        return Duration.ofNanos(durations.stream()
                .mapToLong(Duration::toNanos)
                .min()
                .orElse(0));
    }

    /**
     * Calculates the maximum of a list of durations
     */
    private static Duration calculateMaximum(List<Duration> durations) {
        return Duration.ofNanos(durations.stream()
                .mapToLong(Duration::toNanos)
                .max()
                .orElse(0));
    }

    /**
     * Record to store benchmark results
     */
    public record BenchmarkResult(
            String operationName,
            Duration averageTime,
            Duration minimumTime,
            Duration maximumTime
    ) {
        /**
         * Returns the average operations per second based on the average time
         * @param operationCount The number of operations performed
         * @return Operations per second
         */
        public long operationsPerSecond(int operationCount) {
            double seconds = averageTime.toNanos() / 1_000_000_000.0;
            return seconds > 0 ? (long) (operationCount / seconds) : 0;
        }

        @Override
        public String toString() {
            return String.format("%s: avg=%.2fms, min=%.2fms, max=%.2fms",
                    operationName,
                    averageTime.toMillis(),
                    minimumTime.toMillis(),
                    maximumTime.toMillis());
        }
    }
}
