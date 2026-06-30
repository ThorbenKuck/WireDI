package com.wiredi.runtime.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadPoolSchedulerTest {

    @Test
    @Timeout(10)
    void scheduleOnceAtInstant_viaTrigger() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(1);

            ScheduledFuture<?> future = scheduler.schedule(latch::countDown, Trigger.onceIn(10, TimeUnit.MILLISECONDS));

            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(future.isDone()).isTrue();
            assertThat(future.isCancelled()).isFalse();
        }
    }

    @Test
    @Timeout(10)
    void after_oneShot_runsExactlyOnce() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            AtomicInteger runs = new AtomicInteger();
            CountDownLatch latch = new CountDownLatch(1);

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                runs.incrementAndGet();
                latch.countDown();
            }, Trigger.onceIn(100, TimeUnit.MILLISECONDS));

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(250); // keine weitere Ausführung
            assertThat(runs.get()).isEqualTo(1);
            assertThat(future.isDone()).isTrue();
            assertThat(future.isCancelled()).isFalse();
        }
    }

    @Test
    @Timeout(5)
    void after_zeroDuration_runsImmediately() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(1);
            ScheduledFuture<?> future = scheduler.schedule(latch::countDown, Trigger.onceIn(0, TimeUnit.MILLISECONDS));
            assertThat(latch.await(300, TimeUnit.MILLISECONDS)).isTrue();
            assertThat(future.isDone()).isTrue();
        }
    }

    @Test
    @Timeout(5)
    void at_pastInstant_runsImmediately() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(1);
            ScheduledFuture<?> future = scheduler.schedule(latch::countDown, Trigger.onceAt(Instant.now().minusMillis(50)));
            assertThat(latch.await(300, TimeUnit.MILLISECONDS)).isTrue();
            assertThat(future.isDone()).isTrue();
        }
    }

    @Test
    @Timeout(10)
    void scheduleAtFixedRate_viaTrigger_runsMultipleTimes_andCanBeCancelled() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(3);
            AtomicInteger runs = new AtomicInteger();

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                runs.incrementAndGet();
                latch.countDown();
            }, Trigger.every(Duration.ofMillis(100)));

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            future.cancel(true);

            assertThat(future.isCancelled()).isTrue();
            assertThat(runs.get()).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @Timeout(10)
    void fixedRate_startAt_future() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            AtomicLong firstStartMs = new AtomicLong(-1);
            CountDownLatch latch = new CountDownLatch(1);

            Instant start = Instant.now().plusMillis(200);
            Trigger trigger = Trigger.every(Duration.ofMillis(100), start);

            long scheduledAt = System.currentTimeMillis();
            ScheduledFuture<?> future = scheduler.schedule(() -> {
                firstStartMs.compareAndSet(-1, System.currentTimeMillis());
                latch.countDown();
            }, trigger);

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            long delayMs = firstStartMs.get() - scheduledAt;
            assertThat(delayMs).isGreaterThanOrEqualTo(150);
            future.cancel(true);
        }
    }

    @Test
    @Timeout(10)
    void fixedRate_startAt_past() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(1);
            Instant past = Instant.now().minusMillis(50);
            Trigger trigger = Trigger.every(Duration.ofMillis(200), past);

            ScheduledFuture<?> future = scheduler.schedule(latch::countDown, trigger);
            assertThat(latch.await(500, TimeUnit.MILLISECONDS)).isTrue();
            future.cancel(true);
        }
    }

    @Test
    @Timeout(10)
    void scheduleWithFixedDelay_viaTrigger_runsMultipleTimes_andCanBeCancelled() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(3);
            AtomicInteger runs = new AtomicInteger();

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                runs.incrementAndGet();
                latch.countDown();
            }, Trigger.withFixedDelay(Duration.ofMillis(100)));

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            future.cancel(true);

            assertThat(future.isCancelled()).isTrue();
            assertThat(runs.get()).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @Timeout(10)
    void fixedDelay_respectsCompletionGap() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            ConcurrentLinkedQueue<Long> starts = new ConcurrentLinkedQueue<>();
            CountDownLatch latch = new CountDownLatch(3);

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                starts.add(System.nanoTime());
                try {
                    Thread.sleep(120);
                } catch (InterruptedException ignored) {
                }
                latch.countDown();
            }, Trigger.withFixedDelay(Duration.ofMillis(100)));

            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
            future.cancel(true);

            Long[] times = starts.toArray(new Long[0]);
            for (int i = 1; i < times.length; i++) {
                long deltaMs = (times[i] - times[i - 1]) / 1_000_000L;
                assertThat(deltaMs).isGreaterThanOrEqualTo(180);
            }
        }
    }

    @Test
    @Timeout(10)
    void fixedDelay_startAt_future() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            AtomicLong firstStartMs = new AtomicLong(-1);
            CountDownLatch latch = new CountDownLatch(1);

            Instant start = Instant.now().plusMillis(150);
            Trigger trigger = Trigger.withFixedDelay(start, Duration.ofMillis(100));

            long scheduledAt = System.currentTimeMillis();
            ScheduledFuture<?> future = scheduler.schedule(() -> {
                firstStartMs.compareAndSet(-1, System.currentTimeMillis());
                latch.countDown();
            }, trigger);

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            long delayMs = firstStartMs.get() - scheduledAt;
            assertThat(delayMs).isGreaterThanOrEqualTo(100);
            future.cancel(true);
        }
    }

    @Test
    @Timeout(15)
    void scheduleCron_everySecond_runsMultipleTimes_andCanBeCancelled() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(3);
            AtomicInteger runs = new AtomicInteger();

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                runs.incrementAndGet();
                latch.countDown();
            }, Trigger.cron("*/1 * * * * ?"));

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            future.cancel(true);

            assertThat(future.isCancelled()).isTrue();
            assertThat(runs.get()).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @Timeout(10)
    void cron_cancelStopsFurtherExecutions() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            AtomicInteger runs = new AtomicInteger();
            CountDownLatch latch = new CountDownLatch(1);

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                if (runs.incrementAndGet() == 1) {
                    latch.countDown();
                }
            }, Trigger.cron("*/1 * * * * ?"));

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            future.cancel(true);
            int countAfterCancel = runs.get();
            Thread.sleep(1500);
            assertThat(runs.get()).isEqualTo(countAfterCancel);
        }
    }

    @Test
    @Timeout(10)
    void scheduleWithCustomTrigger_once_thenCompletes() throws Exception {
        Trigger oneShot = context -> {
            if (context.isFirstExecution()) {
                return Instant.now().plusMillis(150);
            }
            return null; // stop after first run
        };

        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(1);

            ScheduledFuture<?> future = scheduler.schedule(latch::countDown, oneShot);

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(future.isDone()).isTrue();
            assertThat(future.isCancelled()).isFalse();
        }
    }

    @Test
    @Timeout(5)
    void scheduleWithCustomTrigger_initialNull_returnsCompletedFutureAndDoesNotRun() throws Exception {
        Trigger none = context -> null;

        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(1);
            ScheduledFuture<?> future = scheduler.schedule(latch::countDown, none);
            // sollte nicht ausführen
            assertThat(latch.await(300, TimeUnit.MILLISECONDS)).isFalse();
            assertThat(future.isDone()).isTrue();
        }
    }

    @Test
    @Timeout(10)
    void scheduleWithBuilder_fixedRate_afterDelay() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(3);
            AtomicInteger runs = new AtomicInteger();

            Trigger trigger = Trigger.builder()
                    .startAfter(Duration.ofMillis(100))
                    .atFixedRate(Duration.ofMillis(100));

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                runs.incrementAndGet();
                latch.countDown();
            }, trigger);

            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
            future.cancel(true);

            assertThat(future.isCancelled()).isTrue();
            assertThat(runs.get()).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @Timeout(10)
    void scheduleWithBuilder_cron_withAfter() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            CountDownLatch latch = new CountDownLatch(2);

            Trigger trigger = Trigger.builder()
                    .startAfter(Duration.ofMillis(100))
                    .cron(b -> b.expression("*/1 * * * * ?"));

            ScheduledFuture<?> future = scheduler.schedule(latch::countDown, trigger);

            assertThat(latch.await(3500, TimeUnit.MILLISECONDS)).isTrue();
            future.cancel(true);
            assertThat(future.isCancelled()).isTrue();
        }
    }

    @Test
    @Timeout(10)
    void scheduleWithBuilder_fixedDelay_startAt() throws Exception {
        try (ThreadPoolScheduler scheduler = new ThreadPoolScheduler(1)) {
            AtomicLong firstStartMs = new AtomicLong(-1);
            CountDownLatch latch = new CountDownLatch(1);

            Instant start = Instant.now().plusMillis(200);
            Trigger trigger = Trigger.builder()
                    .startAt(start)
                    .withFixedDelay(Duration.ofMillis(100));

            long scheduledAt = System.currentTimeMillis();
            ScheduledFuture<?> future = scheduler.schedule(() -> {
                firstStartMs.compareAndSet(-1, System.currentTimeMillis());
                latch.countDown();
            }, trigger);

            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            long delayMs = firstStartMs.get() - scheduledAt;
            assertThat(delayMs).isGreaterThanOrEqualTo(150);
            future.cancel(true);
        }
    }
}
