package com.wiredi.test;

import com.wiredi.properties.keys.Key;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.banner.Banner;

public abstract class AbstractIntegrationTest {

    public WireRepository loadWireRepository() {
        WireRepository wireRepository = WireRepository.create();
        wireRepository.environment().setProperty(Banner.SHOW_BANNER_PROPERTY, "false");
        wireRepository.load();
        return wireRepository;
    }

}
