package com.wiredi.runtime.time.timer;

import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.timer.exceptions.TimerAlreadyStartedException;
import com.wiredi.runtime.time.timer.exceptions.TimerNotStartedException;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TimerTest {

    private static final Percentage PRECISION = Percentage.withPercentage(0.5);

    @Nested
    class NanoTimerCase {

        @Test
        public void simpleTimerWorks() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.nano();
            long timeout = 100;

            // Act
            timer.start();
            Thread.sleep(timeout);
            Timed timed = timer.stop();

            // Assert
            long nanos = timed.get(TimeUnit.NANOSECONDS);
            assertThat(nanos).isCloseTo(Duration.ofMillis(timeout).toNanos(), PRECISION);
        }

        @Test
        public void aStartedSimpleTimerCannotBeStarted() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.nano();
            timer.start();

            // Act Assert
            assertThatCode(timer::start).isInstanceOf(TimerAlreadyStartedException.class);
        }

        @Test
        public void aStoppedSimpleTimerCannotBeStopped() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.nano();
            timer.start();
            timer.stop();

            // Act Assert
            assertThatCode(timer::stop).isInstanceOf(TimerNotStartedException.class);
        }

        @Test
        public void aNotStartedSimpleTimerCannotBeStopped() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.nano();

            // Act Assert
            assertThatCode(timer::stop).isInstanceOf(TimerNotStartedException.class);
        }
    }

    @Nested
    class MillisTimerCase {


        @Test
        public void simpleTimerWorks() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.milli();
            long timeout = 100;

            // Act
            timer.start();
            Thread.sleep(timeout);
            Timed timed = timer.stop();

            // Assert
            long nanos = timed.get(TimeUnit.NANOSECONDS);
            assertThat(nanos).isCloseTo(Duration.ofMillis(timeout).toNanos(), PRECISION);
        }

        @Test
        public void aStartedSimpleTimerCannotBeStarted() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.milli();
            timer.start();

            // Act Assert
            assertThatCode(timer::start).isInstanceOf(TimerAlreadyStartedException.class);
        }

        @Test
        public void aStoppedSimpleTimerCannotBeStopped() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.milli();
            timer.start();
            timer.stop();

            // Act Assert
            assertThatCode(timer::stop).isInstanceOf(TimerNotStartedException.class);
        }

        @Test
        public void aNotStartedSimpleTimerCannotBeStopped() throws InterruptedException {
            // Arrange
            DynamicTimer timer = Timer.milli();

            // Act Assert
            assertThatCode(timer::stop).isInstanceOf(TimerNotStartedException.class);
        }
    }

    @Nested
    class ThreadLocalCase {

        @Test
        public void threadLocalTimerWorks() throws InterruptedException {
            // Arrange
            Timer timer = Timer.threadLocal();
            long timeout = 100;

            // Act
            timer.start();
            Thread.sleep(timeout);
            Timed timed = timer.stop();

            // Assert
            long nanos = timed.get(TimeUnit.NANOSECONDS);
            assertThat(nanos).isCloseTo(Duration.ofMillis(timeout).toNanos(), PRECISION);
        }

        @Test
        public void aThreadLocalTimerBehavesThreadLocal() throws InterruptedException {
            // Arrange
            Timer timer = Timer.threadLocal();
            long timeoutA = 100;
            long timeoutB = 250;
            AtomicReference<Timed> threadATime = new AtomicReference<>();
            AtomicReference<Timed> threadBTime = new AtomicReference<>();

            Thread threadA = new Thread(() -> {
                timer.start();
                try {
                    Thread.sleep(timeoutA);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                threadATime.set(timer.stop());
            });
            Thread threadB = new Thread(() -> {
                timer.start();
                try {
                    Thread.sleep(timeoutB);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                threadBTime.set(timer.stop());
            });

            // Act
            threadA.start();
            threadB.start();
            threadA.join();
            threadB.join();

            // Act Assert
            assertThat(threadATime.get().getNanoseconds()).isCloseTo(Duration.ofMillis(timeoutA).toNanos(), PRECISION);
            assertThat(threadBTime.get().getNanoseconds()).isCloseTo(Duration.ofMillis(timeoutB).toNanos(), PRECISION);
        }

        @Test
        public void aStartedThreadLocalTimerCannotBeStarted() throws InterruptedException {
            // Arrange
            ThreadLocalTimer timer = Timer.threadLocal();
            timer.start();

            // Act Assert
            assertThatCode(timer::start).isInstanceOf(TimerAlreadyStartedException.class);
        }

        @Test
        public void aStoppedThreadLocalTimerCannotBeStopped() throws InterruptedException {
            // Arrange
            ThreadLocalTimer timer = Timer.threadLocal();
            timer.start();
            timer.stop();

            // Act Assert
            assertThatCode(timer::stop).isInstanceOf(TimerNotStartedException.class);
        }

        @Test
        public void aNotStartedThreadLocalTimerCannotBeStopped() throws InterruptedException {
            // Arrange
            ThreadLocalTimer timer = Timer.threadLocal();

            // Act Assert
            assertThatCode(timer::stop).isInstanceOf(TimerNotStartedException.class);
        }
    }
}