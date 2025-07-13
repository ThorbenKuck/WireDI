package com.wiredi.test;

import com.wiredi.runtime.WireContainer;

public abstract class AbstractIntegrationTest {

    public WireContainer loadWireRepository() {
        WireContainer wireRepository = WireContainer.create();
        wireRepository.load();
        return wireRepository;
    }

}
