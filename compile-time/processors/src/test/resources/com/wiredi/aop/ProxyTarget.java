package com.wiredi.aop;

import com.wiredi.annotations.Wire;

@Wire
public class ProxyTarget implements Interface {

    @Override
    @Transactional
    public void toProxy() {

    }

    @Override
    public void notToProxy() {
    }
}
