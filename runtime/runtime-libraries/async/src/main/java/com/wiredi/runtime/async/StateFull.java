package com.wiredi.runtime.async;

import com.wiredi.runtime.async.state.State;
import org.jetbrains.annotations.NotNull;

public interface StateFull<T> {

	@NotNull
	State<T> getState();

	default void tearDown() {
	}
}
