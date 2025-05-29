package com.wiredi.test.commands;

import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Wire;

@Wire
@Primary
public class CommandA implements Command {
    @Override
    public void execute() {
        System.out.println("Command A");
    }
}
