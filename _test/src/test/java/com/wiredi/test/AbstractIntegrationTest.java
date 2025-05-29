package com.wiredi.test;

import com.wiredi.runtime.WireRepository;

public abstract class AbstractIntegrationTest {

    public WireRepository loadWireRepository() {
        WireRepository wireRepository = WireRepository.create();
        wireRepository.load();
        return wireRepository;
    }

}
