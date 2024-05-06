package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireRepository;

public interface Disposable {

	void tearDown(WireRepository origin);

}
