package com.wiredi.runtime;

import com.wiredi.annotations.ManualWireCandidate;
import com.wiredi.domain.WireConflictStrategy;
import com.wiredi.environment.Environment;
import com.wiredi.properties.keys.Key;

@ManualWireCandidate
public class WiredTypesProperties {

	private WireConflictStrategy conflictStrategy = WireConflictStrategy.DEFAULT;
	private boolean autoLoad = true;
	private boolean contextCallbacks = true;
	private static final Key AUTO_LOAD_KEY = Key.just("wire-di.wired.autoload");
	private static final Key WIRE_CONFLICT_STRATEGY_KEY = Key.just("wire-di.wired.concurrent-definition-strategy");

	public void load(Environment environment) {
		autoLoad = environment.properties().getBoolean(AUTO_LOAD_KEY, true);
		environment.map(WIRE_CONFLICT_STRATEGY_KEY, WireConflictStrategy::determine)
				.ifPresent(this::setConflictStrategy);
	}

	public WireConflictStrategy conflictStrategy() {
		return conflictStrategy;
	}

	public void setConflictStrategy(WireConflictStrategy conflictStrategy) {
		this.conflictStrategy = conflictStrategy;
	}

	public boolean isAutoLoad() {
		return autoLoad;
	}

	public boolean contextCallbacksEnabled() {
		return contextCallbacks;
	}

	public void setAutoLoad(boolean autoLoad) {
		this.autoLoad = autoLoad;
	}
}
