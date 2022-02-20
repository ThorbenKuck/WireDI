package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.aspects.AspectRepository;

public interface ProxyFactory<T> extends WireCapable {

    T wrap(T t, AspectRepository aspects, WireRepository wireRepository);

}
