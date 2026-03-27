package com.swmschmidt.td.core.gameplay.command;

public record UiActionCommand(
    String playerId,
    long issuedTick,
    String actionId
) implements GameCommand {
    @Override
    public String commandType() {
        return "ui_action";
    }
}
