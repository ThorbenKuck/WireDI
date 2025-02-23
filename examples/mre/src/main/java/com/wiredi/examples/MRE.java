package com.wiredi.examples;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.WireRepository;

public class MRE {

    public static void main(String[] args) {
        WireRepository wireRepository = WireRepository.open();
        wireRepository.get(DependencyA.class);
    }
}

interface Command {
}

@Wire
class DependencyA {
    private final DependencyB dependencyB;

    DependencyA(DependencyB dependencyB) {
        this.dependencyB = dependencyB;
    }
}

@Wire(to = {Command.class})
class DependencyB {
}
