package com.wiredi.test;

import com.wiredi.runtime.WireContainer;

public abstract class AbstractIntegrationTest {

    public WireContainer loadWireRepository() {
        WireContainer wireContainer = WireContainer.create();
        wireContainer.load();
        return wireContainer;
    }

}
