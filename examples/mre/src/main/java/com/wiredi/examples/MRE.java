package com.wiredi.examples;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.time.Timed;

public class MRE {

    public static void main(String[] args) {
        for (int i = 0 ; i < 100 ; i++) {
            Timed.of(() -> {
                WireContainer wireRepository = WireContainer.open();
                Parent a = wireRepository.get(Parent.class);
                System.out.println(a);
            }).then(time -> System.out.println("MRE took " + time));
        }
    }
}

interface Command {
}

@Wire
class Parent {
    private final Command command;

    Parent(Command command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "Parent@" + Integer.toHexString(hashCode()) + "(" + command + ')';
    }
}

@Wire
class Child implements Command {
    @Override
    public String toString() {
        return "Child@" + Integer.toHexString(hashCode());
    }
}
