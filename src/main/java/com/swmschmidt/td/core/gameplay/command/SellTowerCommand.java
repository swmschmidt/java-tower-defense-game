package com.swmschmidt.td.core.gameplay.command;

public record SellTowerCommand(
    String playerId,
    long issuedTick,
    String towerInstanceId
) implements GameCommand {
    @Override
    public String commandType() {
        return "sell_tower";
    }
}
