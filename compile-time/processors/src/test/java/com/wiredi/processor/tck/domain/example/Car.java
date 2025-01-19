package com.wiredi.processor.tck.domain.example;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.properties.Name;
import jakarta.inject.Named;

@Wire
public class Car {

    private Engine engine;

    public Car(Engine engine) {
        this.engine = engine;
    }

    public void drive() {
        // ...
    }

    public Engine getEngine() {
        return engine;
    }
}
