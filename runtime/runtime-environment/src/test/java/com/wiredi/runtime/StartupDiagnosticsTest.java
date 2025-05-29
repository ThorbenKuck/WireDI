package com.wiredi.runtime;

import com.wiredi.runtime.lang.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class StartupDiagnosticsTest {

    private StartupDiagnostics diagnostics;

    @BeforeEach
    public void setUp() {
        diagnostics = new StartupDiagnostics();
    }

    /**
     * Helper method to convert the collection of states to a map for easier testing
     */
    private Map<String, StartupDiagnostics.TimingState> getStateMap() {
        Collection<StartupDiagnostics.TimingState> states = diagnostics.state().children();
        Map<String, StartupDiagnostics.TimingState> stateMap = new HashMap<>();
        for (StartupDiagnostics.TimingState state : states) {
            stateMap.put(state.name(), state);
        }
        return stateMap;
    }

    @Test
    public void testMeasureTimingWithRunnable() throws Exception {
        // Arrange
        String timingName = "test-runnable";
        Duration duration = Duration.ofMillis(100);

        // Act
        diagnostics.measure(timingName, (ThrowingRunnable<Exception>) () -> {
            Thread.sleep(duration);
        });

        // Assert
        Map<String, StartupDiagnostics.TimingState> stateMap = getStateMap();
        assertThat(stateMap).containsKey(timingName);
        assertThat(stateMap.get(timingName).time().get(java.util.concurrent.TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(50);
    }

    @Test
    public void testNestedTimings() throws InterruptedException {
        // Arrange
        String parent = "parent";
        String child1 = "parent.child1";
        String child2 = "parent.child2";
        Duration firstSleep = Duration.ofMillis(100);
        Duration secondSleep = Duration.ofMillis(50);

        // Act
        diagnostics.measure("parent", () -> {
            diagnostics.measure(child1, () -> Thread.sleep(firstSleep));
            diagnostics.measure(child2, () -> Thread.sleep(secondSleep));
        });

        // Assert
        Map<String, StartupDiagnostics.TimingState> stateMap = getStateMap();
        assertThat(stateMap).containsKey(parent);

        StartupDiagnostics.TimingState parentState = stateMap.get(parent);
        long parentMs = parentState.time().get(TimeUnit.MILLISECONDS);
        assertThat(parentMs).isGreaterThanOrEqualTo(firstSleep.toMillis() + secondSleep.toMillis());

        // Check that the child states are also in the map
        assertThat(parentState.getChild(child1).time().get(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(firstSleep.toMillis());
        assertThat(parentState.getChild(child2).time().get(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(secondSleep.toMillis());
    }
}
