package com.swmschmidt.td.core.gameplay.command;

public interface GameCommand {
    String commandType();

    String playerId();

    long issuedTick();
}
