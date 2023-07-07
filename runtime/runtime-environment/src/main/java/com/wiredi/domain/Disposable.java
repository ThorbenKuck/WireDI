package com.wiredi.domain;

import com.wiredi.runtime.WireRepository;

public interface Disposable {

	void tearDown(WireRepository wireRepository);

}
