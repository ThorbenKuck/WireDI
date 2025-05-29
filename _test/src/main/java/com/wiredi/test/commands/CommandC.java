package com.wiredi.test.commands;

import com.wiredi.annotations.Wire;

@Wire
public class CommandC implements Command {
    @Override
    public void execute() {
        System.out.println("Command C");
    }
}
