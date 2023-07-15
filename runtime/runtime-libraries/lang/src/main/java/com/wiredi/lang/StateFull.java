package com.wiredi.lang;

import com.wiredi.lang.async.State;

public interface StateFull<T> {

	State<T> getState();

}
