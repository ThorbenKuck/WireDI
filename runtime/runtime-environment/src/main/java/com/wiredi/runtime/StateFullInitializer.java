package com.wiredi.runtime;

import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.domain.Eager;

import java.util.List;

public interface StateFullInitializer {

    void initialize(WireRepository wireRepository, List<StateFull<?>> stateFulls);

    class ParallelStream implements StateFullInitializer {

        @Override
        public void initialize(WireRepository wireRepository, List<StateFull<?>> stateFulls) {
            stateFulls.parallelStream().forEach(StateFull::getState);
        }
    }
}
