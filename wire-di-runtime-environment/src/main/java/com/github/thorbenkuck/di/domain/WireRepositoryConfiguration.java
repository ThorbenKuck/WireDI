package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.runtime.WireRepository;

public interface WireRepositoryConfiguration {

	void applyTo(WireRepository wireRepository);

}
