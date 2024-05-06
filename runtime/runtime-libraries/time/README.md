# Time

This module contains a collection of classes to accurately measure times.

In the center of this is the [Timed class](src/main/java/com/wiredi/runtime/time/Timed.java).
The [Timed class](src/main/java/com/wiredi/runtime/time/Timed.java) holds nanoseconds and is integrated into the [TimeRenderer](src/main/java/com/wiredi/runtime/time/TimeRenderer.java), which smartly displays human-readable
representations of the nanoseconds.
To construct it, you can use one of the static methods, like this:

```java
import com.wiredi.runtime.time.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasureExample {
    private final Logger logger = LoggerFactory.getLogger(MeasureExample.class);

    public void measureTimeAndLog() throws InterruptedException {
        // This will construct the Timed instance, based on the provided supplier.
        // The resulting Timed instance contains the time that the runnable required to be executed.
        Timed timed = Timed.of(() -> Thread.sleep(1000));
        
        // The toString method will construct a human-readable string, similar to:
        // 1s
        logger.info("Execution took {}", timed);
    }
}
```

If you need to measure executions, that span across runnable, you can use the [Timed class](src/main/java/com/wiredi/runtime/time/timer/Timer.java) api.
The timer builds on top of a TimeContext and allows for measuring of nano- or millisecond times.
You do so like this:

```java
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.timer.Timer;

public class TimerExample {
    // This timer measures elapsed nanoseconds
    private final Timer nanoSeconds = Timer.nano();
    // This timer measures elapsed milliseconds
    private final Timer milliSeconds = Timer.milli();

    public void example() {
        nanoSeconds.start();

        Timed result = nanoSeconds.stop();
    }
}
```

The resulting Timed will contain the elapsed nanoseconds of the timer.

These two timers differ in the way that they measure times.
The nano timer is measuring more precisely at the expense of performance, whilst the milli timer is measuring faster at the expense of precision.

The advantage of the timer class is also that you can use the timer exchangeable and make them thread local.
To do so, you can either create a new ThreadLocal timer, or you can convert a timer, like this:

```java
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.timer.Timer;

public class TimerExample {
    // This timer measures elapsed nanoseconds per thread in nanoseconds
    private final Timer nanoThreadLocal = Timer.threadLocalNano();
    // This timer measures elapsed milliseconds
    private final Timer milliSeconds = Timer.milli();

    public void example() {
        // Convert a timer to a thread local.
        Timer milliThreadLocal = milliSeconds.asThreadLocal();
    }
}
```

Thread local timers are really helpful if you are measuring the same thing in multiple threads.
For example, if you are measuring the elapsed time that a web-request handler takes.
In these scenarios, the code could look like this:

```java
import com.wiredi.runtime.time.timer.Timer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is implementing a hypothetical interface, that is used in a web server
public class LoggingRequestInterceptor implements HttpRequestInterceptor {

    private final Timer timer = Timer.threadLocal();
    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    public void beforeExecution(HttpRequest request, HttpResponse response) {
        timer.start();
    }

    public void afterExecution(HttpRequest request, HttpResponse response, @Nullable Throwable error) {
        logger.info("Execution of {} {} took {}", request.getMethod(), request.getPath(), timer.stop());
    }
}
```
