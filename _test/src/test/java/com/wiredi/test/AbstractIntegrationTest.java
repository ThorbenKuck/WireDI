package com.wiredi.test;

import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WiredApplicationInstance;

public abstract class AbstractIntegrationTest {

    public WiredApplicationInstance startApplication() {
        return WiredApplication.start();
    }

}
