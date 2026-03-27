package com.swmschmidt.td.core.gameplay.command;

public record SelectEntityCommand(
    String playerId,
    long issuedTick,
    String entityType,
    String entityId
) implements GameCommand {
    @Override
    public String commandType() {
        return "select_entity";
    }
}
