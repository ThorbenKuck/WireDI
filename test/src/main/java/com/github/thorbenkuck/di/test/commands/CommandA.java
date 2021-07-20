package com.github.thorbenkuck.di.test.commands;

import com.github.thorbenkuck.di.annotations.Wire;

@Wire
public class CommandA implements Command {
    @Override
    public void execute() {
        System.out.println("Command A");
    }
}
