package com.swmschmidt.td.core.gameplay.command;

import com.swmschmidt.td.core.math.Vector3;

public record MoveBuilderCommand(
    String playerId,
    long issuedTick,
    String builderInstanceId,
    Vector3 targetPosition
) implements GameCommand {
    @Override
    public String commandType() {
        return "move_builder";
    }
}
