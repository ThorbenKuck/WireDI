package com.github.thorbenkuck.di.test.commands;

import com.github.thorbenkuck.di.annotations.WirePriority;
import com.github.thorbenkuck.di.annotations.Wire;

import javax.inject.Singleton;

@Wire
@WirePriority(WirePriority.DEFAULT + 1)
@Singleton
public class CommandB implements Command {
    @Override
    public void execute() {
        System.out.println("Command B");
    }
}
