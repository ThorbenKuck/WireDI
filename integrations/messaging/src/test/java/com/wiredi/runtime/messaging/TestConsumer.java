package com.wiredi.runtime.messaging;

import com.wiredi.annotations.Wire;

import java.util.function.Consumer;

@Wire
public class TestConsumer implements Consumer<String> {
    @Override
    public void accept(String s) {

    }
}
