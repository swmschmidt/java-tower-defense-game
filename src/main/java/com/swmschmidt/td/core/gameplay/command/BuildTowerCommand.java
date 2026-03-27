package com.swmschmidt.td.core.gameplay.command;

import com.swmschmidt.td.core.math.Vector3;

public record BuildTowerCommand(
    String playerId,
    long issuedTick,
    String builderInstanceId,
    String towerDefinitionId,
    Vector3 targetPosition
) implements GameCommand {
    @Override
    public String commandType() {
        return "build_tower";
    }
}
