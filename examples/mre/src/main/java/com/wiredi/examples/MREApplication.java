package com.wiredi.examples;

import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.time.Timed;

public class MREApplication {

    public static void main(String[] args) {
        Timed.of(() -> WiredApplication.start())
                .then(timed -> System.out.println("Application started in " + timed.time()));
    }
}
