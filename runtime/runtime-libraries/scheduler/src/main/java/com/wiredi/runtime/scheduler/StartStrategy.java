package com.wiredi.runtime.scheduler;

import java.time.Instant;

public interface StartStrategy {

    Instant resolveBase();

    StartStrategy IMMEDIATE = Instant::now;
}