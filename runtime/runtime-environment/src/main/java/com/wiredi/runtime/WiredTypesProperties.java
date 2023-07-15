package com.wiredi.runtime;

import com.wiredi.annotations.ManualWireCandidate;
import com.wiredi.domain.WireConflictStrategy;
import com.wiredi.environment.Environment;
import com.wiredi.properties.TypedProperties;
import com.wiredi.properties.keys.Key;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@ManualWireCandidate
public class WiredTypesProperties {

	private WireConflictStrategy conflictStrategy = WireConflictStrategy.DEFAULT;
	private boolean contextCallbacks = true;
	private boolean loadEagerInstance = true;
	private boolean awaitStates = true;
	private Duration awaitStatesTimeout = Duration.of(30, ChronoUnit.SECONDS);
	private static final Key WIRE_CONFLICT_STRATEGY_KEY = Key.just("wire-di.wired.concurrent-definition-strategy");
	private static final Key CONTEXT_CALLBACKS_KEY = Key.just("wire-di.startup.context-callbacks-enabled");
	private static final Key LOAD_EAGER_INSTANCE = Key.just("wire-di.startup.load-eager-instances");
	private static final Key AWAIT_STATES = Key.just("wire-di.startup.await-states");
	private static final Key AWAIT_STATES_TIMEOUT = Key.just("wire-di.startup.await-states-timeout");

	public void load(Environment environment) {
		TypedProperties properties = environment.properties();

		contextCallbacks = properties.getBoolean(CONTEXT_CALLBACKS_KEY, contextCallbacks);
		loadEagerInstance = properties.getBoolean(LOAD_EAGER_INSTANCE, loadEagerInstance);
		awaitStates = properties.getBoolean(AWAIT_STATES, awaitStates);
		awaitStatesTimeout = environment.map(AWAIT_STATES_TIMEOUT, Duration::parse)
						.orElse(awaitStatesTimeout);

		conflictStrategy = environment.map(WIRE_CONFLICT_STRATEGY_KEY, WireConflictStrategy::determine)
				.orElse(conflictStrategy);
	}

	public WireConflictStrategy conflictStrategy() {
		return conflictStrategy;
	}

	public void setConflictStrategy(WireConflictStrategy conflictStrategy) {
		this.conflictStrategy = conflictStrategy;
	}

	public boolean contextCallbacksEnabled() {
		return contextCallbacks;
	}

	public Duration awaitStatesTimeout() {
		return awaitStatesTimeout;
	}

	public void setAwaitStatesTimeout(Duration timeout) {
		this.awaitStatesTimeout = timeout;
	}

	public void setContextCallbacksEnabled(boolean enabled) {
		this.contextCallbacks = enabled;
	}

	public boolean loadEagerInstance() {
		return loadEagerInstance;
	}

	public void setLoadEagerInstance(boolean loadEagerInstance) {
		this.loadEagerInstance = loadEagerInstance;
	}

	public boolean awaitStates() {
		return awaitStates;
	}

	public void setAwaitStates(boolean awaitStates) {
		this.awaitStates = awaitStates;
	}
}
