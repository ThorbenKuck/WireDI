package com.wiredi.test.commands;

import com.wiredi.annotations.WirePriority;
import com.wiredi.annotations.Wire;

import jakarta.inject.Singleton;

@Wire
@WirePriority(WirePriority.DEFAULT + 1)
@Singleton
public class CommandB implements Command {
    @Override
    public void execute() {
        System.out.println("Command B");
    }
}
